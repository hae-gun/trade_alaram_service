// 카카오 OAuth 로그인 유스케이스입니다.
// 외부 카카오 계정을 내부 사용자 계정으로 생성하거나 기존 계정과 매핑합니다.
package com.tradealarm.domain.auth.application

import com.tradealarm.domain.auth.infra.KakaoAuthClient
import com.tradealarm.domain.auth.infra.KakaoProperties
import com.tradealarm.domain.user.domain.AuthProvider
import com.tradealarm.domain.user.domain.User
import com.tradealarm.domain.user.domain.UserRepository
import com.tradealarm.global.exception.ApiException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class KakaoLoginCommand(
    val authorizationCode: String,
    val redirectUri: String?,
)

data class KakaoLoginResult(
    val user: User,
    val isNewUser: Boolean,
)

@Service
class KakaoLoginService(
    private val kakaoAuthClient: KakaoAuthClient,
    private val kakaoProperties: KakaoProperties,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun login(command: KakaoLoginCommand): KakaoLoginResult {
        val redirectUri = command.redirectUri?.takeIf { it.isNotBlank() } ?: kakaoProperties.redirectUri
        if (redirectUri.isBlank()) {
            throw ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 리다이렉트 URI 설정이 필요합니다.")
        }

        val token = kakaoAuthClient.requestAccessToken(command.authorizationCode, redirectUri)
        val kakaoUser = kakaoAuthClient.fetchUser(token.accessToken)
        val providerUserId = kakaoUser.id.toString()

        val existingUser = userRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, providerUserId)
        if (existingUser.isPresent) {
            return KakaoLoginResult(existingUser.get(), isNewUser = false)
        }

        val savedUser = userRepository.save(
            User(
                email = kakaoUser.email ?: "kakao-$providerUserId@tradealarm.local",
                nickname = kakaoUser.nickname ?: "kakao-$providerUserId",
                provider = AuthProvider.KAKAO,
                providerUserId = providerUserId,
            ),
        )

        return KakaoLoginResult(savedUser, isNewUser = true)
    }
}
