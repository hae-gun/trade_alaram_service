// 관심종목과 활성 알림 종목의 현재가를 백그라운드에서 주기적으로 갱신합니다.
// 사용자 API 요청에서는 KIS를 직접 호출하지 않고, 이 스케줄러가 저장한 스냅샷을 읽습니다.
package com.tradealarm.domain.market.application

import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.watchlist.domain.WatchlistRepository
import com.tradealarm.global.lock.DistributedLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class MarketPriceRefreshScheduler(
    private val watchlistRepository: WatchlistRepository,
    private val alertRuleRepository: AlertRuleRepository,
    private val marketPriceService: MarketPriceService,
    private val distributedLock: DistributedLock,
    @Value("\${app.market.request-delay-ms:1500}")
    private val requestDelayMs: Long,
    @Value("\${app.market.error-backoff-ms:5000}")
    private val errorBackoffMs: Long,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(
        fixedDelayString = "\${app.market.refresh-delay-ms:60000}",
        initialDelayString = "\${app.market.initial-delay-ms:10000}",
    )
    fun refresh() {
        val lockKey = "tradealarm:lock:market-price-refresh"
        if (!distributedLock.acquire(lockKey, Duration.ofSeconds(55))) {
            return
        }

        try {
            collectTargetStocks().forEachIndexed { index, stock ->
                if (index > 0 && requestDelayMs > 0) {
                    Thread.sleep(requestDelayMs)
                }
                if (!refreshSafely(stock) && errorBackoffMs > 0) {
                    Thread.sleep(errorBackoffMs)
                }
            }
        } finally {
            distributedLock.release(lockKey)
        }
    }

    private fun collectTargetStocks(): List<Stock> {
        return (watchlistRepository.findDistinctEnabledStocks() + alertRuleRepository.findDistinctEnabledStocks())
            .distinctBy { it.id }
            .sortedBy { it.symbol }
    }

    private fun refreshSafely(stock: Stock): Boolean {
        return runCatching {
            marketPriceService.refreshPrice(stock)
        }.onFailure { error ->
            log.warn("현재가 갱신 실패: symbol={}, reason={}", stock.symbol, error.message)
        }.isSuccess
    }
}
