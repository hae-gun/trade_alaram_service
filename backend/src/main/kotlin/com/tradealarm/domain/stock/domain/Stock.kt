// 주식 종목 마스터 도메인 모델입니다.
// 시장 구분, 종목 코드, 종목명을 저장하고 관심종목/알림 규칙의 기준 데이터로 사용합니다.
package com.tradealarm.domain.stock.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

enum class Market {
    KOSPI,
    KOSDAQ,
    KONEX,
}

@Entity
@Table(name = "stocks")
class Stock(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var market: Market,

    @Column(nullable = false, unique = true, length = 16)
    var symbol: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)
