// 소셜 로그인 HTTP API입니다.
// 카카오 인가 코드를 받아 백엔드에서 카카오 사용자 정보를 조회하고 내부 사용자와 매핑합니다.
package com.tradealarm.domain.auth.api

import com.tradealarm.domain.auth.application.KakaoLoginCommand
import com.tradealarm.domain.auth.application.KakaoLoginService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class KakaoLoginRequest(
    @field:NotBlank(message = "카카오 인가 코드는 필수입니다.")
    val authorizationCode: String,

    val redirectUri: String? = null,
)

data class KakaoLoginResponse(
    val user: AuthenticatedUserResponse,
    val provider: String,
    val isNewUser: Boolean,
)

data class AuthenticatedUserResponse(
    val id: String,
    val email: String,
    val nickname: String,
)

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val kakaoLoginService: KakaoLoginService,
) {
    @PostMapping("/kakao/login")
    @ResponseStatus(HttpStatus.OK)
    fun kakaoLogin(
        @Valid @RequestBody request: KakaoLoginRequest,
    ): KakaoLoginResponse {
        val result = kakaoLoginService.login(
            KakaoLoginCommand(
                authorizationCode = request.authorizationCode,
                redirectUri = request.redirectUri,
            ),
        )

        return KakaoLoginResponse(
            user = AuthenticatedUserResponse(
                id = result.user.id.toString(),
                email = result.user.email,
                nickname = result.user.nickname,
            ),
            provider = result.user.provider.name,
            isNewUser = result.isNewUser,
        )
    }
}
