// 사용자 엔티티의 JPA 저장소입니다.
// 로그인/데모 사용자 조회를 위해 이메일 기반 조회 메서드를 제공합니다.
package com.tradealarm.domain.user.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): Optional<User>

    fun findByProviderAndProviderUserId(provider: AuthProvider, providerUserId: String): Optional<User>
}
