// 종목 검색/조회 HTTP API입니다.
// 프론트엔드의 종목 검색 패널과 관심종목 추가 흐름에서 사용됩니다.
package com.tradealarm.domain.stock.api

import com.tradealarm.domain.stock.application.StockService
import com.tradealarm.domain.stock.domain.Market
import com.tradealarm.domain.stock.domain.Stock
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class StockResponse(
    val id: String,
    val market: Market,
    val symbol: String,
    val name: String,
)

@RestController
@RequestMapping("/api/stocks")
class StockController(
    private val stockService: StockService,
) {
    @GetMapping
    fun search(@RequestParam(required = false) query: String?): List<StockResponse> {
        return stockService.search(query).map { it.toResponse() }
    }

    @GetMapping("/{stockId}")
    fun get(@PathVariable stockId: UUID): StockResponse {
        return stockService.get(stockId).toResponse()
    }
}

fun Stock.toResponse(): StockResponse {
    return StockResponse(
        id = id.toString(),
        market = market,
        symbol = symbol,
        name = name,
    )
}
