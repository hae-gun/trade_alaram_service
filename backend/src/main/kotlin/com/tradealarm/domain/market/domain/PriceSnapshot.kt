// 특정 시점의 종목 가격 스냅샷 모델입니다.
// 알림 조건 평가와 발송 이력에 사용할 현재가/등락률 데이터를 저장합니다.
package com.tradealarm.domain.market.domain

import com.tradealarm.domain.stock.domain.Stock
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "price_snapshots")
class PriceSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    var stock: Stock,

    @Column(nullable = false, precision = 19, scale = 2)
    var price: BigDecimal,

    @Column(nullable = false, precision = 8, scale = 3)
    var changeRate: BigDecimal,

    @Column(nullable = false)
    var capturedAt: Instant = Instant.now(),
)
