// 시장 데이터 HTTP API입니다.
// 관심종목과 알림 조건 화면에서 특정 종목의 현재가를 조회하는 데 사용합니다.
package com.tradealarm.domain.market.api

import com.tradealarm.domain.market.application.MarketPriceService
import com.tradealarm.domain.stock.application.StockService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PriceResponse(
    val stockId: String,
    val price: BigDecimal,
    val changeRate: BigDecimal,
    val capturedAt: Instant,
)

@RestController
@RequestMapping("/api/market")
class MarketController(
    private val stockService: StockService,
    private val marketPriceService: MarketPriceService,
) {
    @GetMapping("/stocks/{stockId}/price")
    fun price(@PathVariable stockId: UUID): PriceResponse {
        val stock = stockService.get(stockId)
        val snapshot = marketPriceService.getCachedPrice(stock)
        return PriceResponse(
            stockId = stock.id.toString(),
            price = snapshot.price,
            changeRate = snapshot.changeRate,
            capturedAt = snapshot.capturedAt,
        )
    }
}
