// 관심종목 HTTP API입니다.
// 목록 응답에는 최신 가격 스냅샷을 함께 실어 대시보드에서 바로 표시할 수 있게 합니다.
package com.tradealarm.domain.watchlist.api

import com.tradealarm.domain.market.application.MarketPriceService
import com.tradealarm.domain.stock.api.StockResponse
import com.tradealarm.domain.stock.api.toResponse
import com.tradealarm.domain.watchlist.application.WatchlistService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AddWatchlistRequest(
    @field:NotNull(message = "stockId is required.")
    val stockId: UUID?,
)

data class WatchlistItemResponse(
    val id: String,
    val stock: StockResponse,
    val currentPrice: BigDecimal,
    val changeRate: BigDecimal,
    val createdAt: Instant,
)

@RestController
@RequestMapping("/api/watchlist")
class WatchlistController(
    private val watchlistService: WatchlistService,
    private val marketPriceService: MarketPriceService,
) {
    @GetMapping
    fun list(): List<WatchlistItemResponse> {
        return watchlistService.getMyWatchlist().map { item ->
            val snapshot = marketPriceService.getCachedPrice(item.stock)
            WatchlistItemResponse(
                id = item.id.toString(),
                stock = item.stock.toResponse(),
                currentPrice = snapshot.price,
                changeRate = snapshot.changeRate,
                createdAt = item.createdAt,
            )
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@Valid @RequestBody request: AddWatchlistRequest): WatchlistItemResponse {
        val item = watchlistService.add(request.stockId!!)
        val snapshot = marketPriceService.getCachedPrice(item.stock)
        return WatchlistItemResponse(
            id = item.id.toString(),
            stock = item.stock.toResponse(),
            currentPrice = snapshot.price,
            changeRate = snapshot.changeRate,
            createdAt = item.createdAt,
        )
    }

    @DeleteMapping("/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable stockId: UUID) {
        watchlistService.remove(stockId)
    }
}
