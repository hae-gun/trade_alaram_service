// 시세 조회 유스케이스를 담당합니다.
// 한국투자증권 Open API 연동 전까지는 종목 코드 기반 mock 가격을 생성합니다.
package com.tradealarm.domain.market.application

import com.tradealarm.domain.market.domain.PriceSnapshot
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.stock.domain.Stock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

@Service
class MarketPriceService(
    private val priceSnapshotRepository: PriceSnapshotRepository,
) {
    @Transactional
    fun getCurrentPrice(stock: Stock): PriceSnapshot {
        val latest = priceSnapshotRepository.findTopByStockOrderByCapturedAtDesc(stock)
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
