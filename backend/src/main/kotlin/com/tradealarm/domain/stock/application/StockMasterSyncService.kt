// 전체 종목 마스터를 외부 파일에서 동기화해 검색 DB를 최신 상태로 유지합니다.
package com.tradealarm.domain.stock.application

import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.stock.domain.StockRepository
import com.tradealarm.domain.stock.infra.KrxStockMasterClient
import com.tradealarm.domain.stock.infra.StockMasterItem
import com.tradealarm.domain.stock.infra.StockMasterProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class StockMasterSyncService(
    private val stockRepository: StockRepository,
    private val stockMasterProperties: StockMasterProperties,
    private val krxStockMasterClient: KrxStockMasterClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun sync(): StockMasterSyncResult {
        if (!stockMasterProperties.enabled) {
            return StockMasterSyncResult(0, 0, 0, 0, true)
        }

        val items = stockMasterProperties.sources.flatMapIndexed { index, source ->
            if (index > 0 && stockMasterProperties.requestDelayMs > 0) {
                Thread.sleep(stockMasterProperties.requestDelayMs)
            }

            runCatching {
                krxStockMasterClient.fetch(source)
            }.onFailure { error ->
                log.warn("종목 마스터 조회 실패: market={}, reason={}", source.market, error.message)
            }.getOrDefault(emptyList())
        }.distinctBy { it.symbol }

        if (items.isEmpty()) {
            return StockMasterSyncResult(0, 0, 0, 0, false)
        }

        val stocksBySymbol = stockRepository.findAll().associateBy { it.symbol }
        val itemSymbols = items.map { it.symbol }.toSet()
        val now = Instant.now()
        var created = 0
        var updated = 0
        var disabled = 0

        val stocks = items.map { item ->
            val existing = stocksBySymbol[item.symbol]
            if (existing == null) {
                created += 1
                item.toStock(now)
            } else {
                if (existing.name != item.name || existing.market != item.market || !existing.enabled) {
                    updated += 1
                }
                existing.market = item.market
                existing.name = item.name
                existing.enabled = true
                existing.updatedAt = now
                existing
            }
        }

        val staleStocks = stocksBySymbol.values
            .filter { stock -> stock.enabled && stock.symbol !in itemSymbols }
            .onEach { stock ->
                stock.enabled = false
                stock.updatedAt = now
                disabled += 1
            }

        stockRepository.saveAll(stocks + staleStocks)
        log.info(
            "종목 마스터 동기화 완료: total={}, created={}, updated={}, disabled={}",
            items.size,
            created,
            updated,
            disabled,
        )
        return StockMasterSyncResult(items.size, created, updated, disabled, true)
    }

    private fun StockMasterItem.toStock(now: Instant): Stock {
        return Stock(
            market = market,
            symbol = symbol,
            name = name,
            enabled = true,
            updatedAt = now,
        )
    }
}

data class StockMasterSyncResult(
    val total: Int,
    val created: Int,
    val updated: Int,
    val disabled: Int,
    val success: Boolean,
)
