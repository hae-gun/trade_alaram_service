// 사용자별 관심종목 모델입니다.
// 한 사용자가 같은 종목을 중복 등록하지 않도록 user_id와 stock_id에 unique 제약을 둡니다.
package com.tradealarm.domain.watchlist.domain

import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.user.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "watchlist_items",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "stock_id"])],
)
class WatchlistItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    var stock: Stock,

    var createdAt: Instant = Instant.now(),
)
