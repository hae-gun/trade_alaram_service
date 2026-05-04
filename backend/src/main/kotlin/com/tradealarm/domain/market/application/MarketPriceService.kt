// 시세 조회 유스케이스를 담당합니다.
// API 요청은 저장된 최신 스냅샷만 읽고, 외부 시세 갱신은 스케줄러에서 별도로 수행합니다.
package com.tradealarm.domain.market.application

import com.tradealarm.domain.market.domain.PriceSnapshot
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.market.infra.KisCurrentPriceClient
import com.tradealarm.domain.stock.domain.Stock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

@Service
class MarketPriceService(
    private val priceSnapshotRepository: PriceSnapshotRepository,
    private val kisCurrentPriceClient: KisCurrentPriceClient,
) {
    @Transactional
    fun getCurrentPrice(stock: Stock): PriceSnapshot {
        return getCachedPrice(stock)
    }

    @Transactional
    fun getCachedPrice(stock: Stock): PriceSnapshot {
        val latest = priceSnapshotRepository.findTopByStockOrderByCapturedAtDesc(stock)
        if (latest.isPresent) {
            return latest.get()
        }

        return priceSnapshotRepository.save(createMockSnapshot(stock))
    }

    @Transactional
    fun refreshPrice(stock: Stock): PriceSnapshot {
        if (kisCurrentPriceClient.isEnabled()) {
            val currentPrice = kisCurrentPriceClient.getCurrentPrice(stock.symbol)
            return priceSnapshotRepository.save(
                PriceSnapshot(
                    stock = stock,
                    price = currentPrice.price,
                    changeRate = currentPrice.changeRate,
                ),
            )
        }

        return priceSnapshotRepository.save(createMockSnapshot(stock))
    }

    private fun createMockSnapshot(stock: Stock): PriceSnapshot {
        return PriceSnapshot(
            stock = stock,
            price = mockBasePrice(stock.symbol),
            changeRate = mockChangeRate(stock.symbol),
        )
    }

    private fun mockBasePrice(symbol: String): BigDecimal {
        val seed = abs(symbol.hashCode())
        return BigDecimal(10_000 + seed % 90_000).setScale(2, RoundingMode.HALF_UP)
    }

    private fun mockChangeRate(symbol: String): BigDecimal {
        val seed = abs(symbol.hashCode() % 600)
        return BigDecimal(seed - 300).divide(BigDecimal(100), 3, RoundingMode.HALF_UP)
    }
}
