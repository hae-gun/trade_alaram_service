// 시세 조회 유스케이스를 담당합니다.
// KIS 연동이 켜져 있으면 실제 현재가를 저장하고, 꺼져 있으면 기존 mock 가격 흐름을 유지합니다.
package com.tradealarm.domain.market.application

import com.tradealarm.domain.market.domain.PriceSnapshot
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.market.infra.KisCurrentPriceClient
import com.tradealarm.domain.stock.domain.Stock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Service
class MarketPriceService(
    private val priceSnapshotRepository: PriceSnapshotRepository,
    private val kisCurrentPriceClient: KisCurrentPriceClient,
) {
    @Transactional
    fun getCurrentPrice(stock: Stock): PriceSnapshot {
        val latest = priceSnapshotRepository.findTopByStockOrderByCapturedAtDesc(stock)
        if (latest.isPresent && latest.get().capturedAt.isAfter(Instant.now().minus(30, ChronoUnit.SECONDS))) {
            return latest.get()
        }

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

        if (latest.isPresent) {
            return latest.get()
        }

        val basePrice = mockBasePrice(stock.symbol)
        val snapshot = PriceSnapshot(
            stock = stock,
            price = basePrice,
            changeRate = mockChangeRate(stock.symbol),
        )
        return priceSnapshotRepository.save(snapshot)
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
