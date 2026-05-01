// 관심종목 등록/조회/삭제 유스케이스를 담당합니다.
// 현재는 데모 사용자를 기준으로 사용자별 관심종목을 관리합니다.
package com.tradealarm.domain.watchlist.application

import com.tradealarm.domain.stock.application.StockService
import com.tradealarm.domain.user.application.DemoUserService
import com.tradealarm.domain.watchlist.domain.WatchlistItem
import com.tradealarm.domain.watchlist.domain.WatchlistRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WatchlistService(
    private val demoUserService: DemoUserService,
    private val stockService: StockService,
    private val watchlistRepository: WatchlistRepository,
) {
    @Transactional(readOnly = true)
    fun getMyWatchlist(): List<WatchlistItem> {
        val user = demoUserService.getOrCreateDemoUser()
        return watchlistRepository.findAllByUserOrderByCreatedAtDesc(user)
    }

    @Transactional
    fun add(stockId: UUID): WatchlistItem {
        val user = demoUserService.getOrCreateDemoUser()
        val stock = stockService.get(stockId)

        if (watchlistRepository.existsByUserAndStock_Id(user, stockId)) {
            return watchlistRepository.findAllByUserOrderByCreatedAtDesc(user)
                .first { it.stock.id == stockId }
        }

        return watchlistRepository.save(WatchlistItem(user = user, stock = stock))
    }

    @Transactional
    fun remove(stockId: UUID) {
        val user = demoUserService.getOrCreateDemoUser()
        watchlistRepository.deleteByUserAndStock_Id(user, stockId)
    }
}
