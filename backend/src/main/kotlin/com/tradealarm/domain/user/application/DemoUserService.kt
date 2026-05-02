// 인증 연동 전 단계에서 사용할 admin 사용자를 보장하는 서비스입니다.
// 모든 MVP API는 우선 이 admin 사용자를 기준으로 H2 DB에 사용자별 데이터를 분리합니다.
package com.tradealarm.domain.user.application

import com.tradealarm.domain.user.domain.AuthProvider
import com.tradealarm.domain.user.domain.User
import com.tradealarm.domain.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DemoUserService(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun getOrCreateDemoUser(): User {
        return userRepository.findByEmail(ADMIN_EMAIL).orElseGet {
            userRepository.save(
                User(
                    email = ADMIN_EMAIL,
                    nickname = "admin",
                    provider = AuthProvider.DEMO,
                    providerUserId = "admin",
                ),
            )
        }
    }

    companion object {
        const val ADMIN_EMAIL = "admin@tradealarm.local"
    }
}
