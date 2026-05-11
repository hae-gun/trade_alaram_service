// 가격/등락률 알림 조건 도메인 모델입니다.
// 사용자가 설정한 조건과 중복 발송 방지를 위한 마지막 발송 시각을 함께 관리합니다.
package com.tradealarm.domain.alert.domain

import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.user.domain.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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

enum class AlertType {
    ABOVE_PRICE,
    BELOW_PRICE,
    UP_RATE,
    DOWN_RATE,
}

enum class RepeatPolicy {
    ONCE,
    COOLDOWN,
}

@Entity
@Table(name = "alert_rules")
class AlertRule(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    var stock: Stock,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: AlertType,

    @Column(precision = 19, scale = 2)
    var targetPrice: BigDecimal? = null,

    @Column(precision = 8, scale = 3)
    var changeRate: BigDecimal? = null,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var deleted: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var repeatPolicy: RepeatPolicy = RepeatPolicy.ONCE,

    var lastTriggeredAt: Instant? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
) {
    // 현재가와 등락률이 이 규칙의 조건을 만족하는지 판단합니다.
    fun isTriggered(currentPrice: BigDecimal, currentChangeRate: BigDecimal): Boolean {
        return when (type) {
            AlertType.ABOVE_PRICE -> targetPrice != null && currentPrice >= targetPrice
            AlertType.BELOW_PRICE -> targetPrice != null && currentPrice <= targetPrice
            AlertType.UP_RATE -> changeRate != null && currentChangeRate >= changeRate
            AlertType.DOWN_RATE -> changeRate != null && currentChangeRate <= changeRate!!.negate()
        }
    }
}
