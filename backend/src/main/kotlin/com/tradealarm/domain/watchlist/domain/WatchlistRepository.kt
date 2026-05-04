// 관심종목 JPA 저장소입니다.
// 사용자별 목록 조회, 중복 확인, 삭제 기능을 제공합니다.
package com.tradealarm.domain.watchlist.domain

import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.user.domain.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface WatchlistRepository : JpaRepository<WatchlistItem, UUID> {
    @EntityGraph(attributePaths = ["stock"])
    fun findAllByUserOrderByCreatedAtDesc(user: User): List<WatchlistItem>
    fun existsByUserAndStock_Id(user: User, stockId: UUID): Boolean
    fun deleteByUserAndStock_Id(user: User, stockId: UUID)

    @Query("select distinct item.stock from WatchlistItem item where item.stock.enabled = true")
    fun findDistinctEnabledStocks(): List<Stock>
}
