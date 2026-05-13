// 알림 채널과 알림 발송 이벤트 도메인 모델입니다.
// 이메일/카카오/Web Push 확장을 고려해 채널과 실제 발송 이력을 분리합니다.
package com.tradealarm.domain.notification.domain

import com.tradealarm.domain.alert.domain.AlertRule
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

enum class NotificationChannelType {
    EMAIL,
    SLACK,
    KAKAO_ALIMTALK,
    WEB_PUSH,
}

enum class NotificationStatus {
    PENDING,
    SENT,
    FAILED,
}

@Entity
@Table(name = "notification_channels")
class NotificationChannel(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: NotificationChannelType,

    @Column(nullable = false)
    var destination: String,

    @Column(nullable = false)
    var verified: Boolean = false,

    @Column(nullable = false)
    var enabled: Boolean = true,
)

@Entity
@Table(name = "alert_events")
class AlertEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_rule_id", nullable = false)
    var alertRule: AlertRule,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    var stock: Stock,

    @Column(nullable = false, precision = 19, scale = 2)
    var triggerPrice: BigDecimal,

    @Column(nullable = false, precision = 8, scale = 3)
    var triggerChangeRate: BigDecimal,

    @Column(nullable = false)
    var message: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: NotificationStatus = NotificationStatus.SENT,

    @Column(nullable = false)
    var sentAt: Instant = Instant.now(),
)
