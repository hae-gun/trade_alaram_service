// 사용자 도메인 모델입니다.
// 소셜 로그인 공급자와 공급자별 사용자 식별자를 함께 저장해 계정 매핑의 기준으로 사용합니다.
package com.tradealarm.domain.user.domain

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

enum class AuthProvider {
    GOOGLE,
    KAKAO,
    NAVER,
    DEMO,
}

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AuthProvider,

    @Column(nullable = false)
    var providerUserId: String,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
)
