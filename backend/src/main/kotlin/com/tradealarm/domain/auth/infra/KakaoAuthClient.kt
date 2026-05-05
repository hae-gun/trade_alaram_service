// 카카오 OAuth API 클라이언트 계약과 HTTP 구현입니다.
// 앱 키와 시크릿은 환경변수 기반 설정으로만 주입합니다.
package com.tradealarm.domain.auth.infra

import com.fasterxml.jackson.annotation.JsonProperty
import com.tradealarm.global.exception.ApiException
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@ConfigurationProperties(prefix = "app.kakao")
data class KakaoProperties(
    val authBaseUrl: String = "https://kauth.kakao.com",
    val apiBaseUrl: String = "https://kapi.kakao.com",
    val restApiKey: String = "",
    val clientSecret: String = "",
    val redirectUri: String = "",
) {
    fun isConfigured(): Boolean = restApiKey.isNotBlank()
}

data class KakaoAccessToken(
    val accessToken: String,
)

data class KakaoUserProfile(
    val id: Long,
    val email: String?,
    val nickname: String?,
)

interface KakaoAuthClient {
    fun requestAccessToken(authorizationCode: String, redirectUri: String): KakaoAccessToken

    fun fetchUser(accessToken: String): KakaoUserProfile
}

@Component
class KakaoHttpAuthClient(
    private val restClientBuilder: RestClient.Builder,
    private val kakaoProperties: KakaoProperties,
) : KakaoAuthClient {
    override fun requestAccessToken(authorizationCode: String, redirectUri: String): KakaoAccessToken {
        if (!kakaoProperties.isConfigured()) {
            throw ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 REST API 키 설정이 필요합니다.")
        }

        val form = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoProperties.restApiKey)
            add("redirect_uri", redirectUri)
            add("code", authorizationCode)
            if (kakaoProperties.clientSecret.isNotBlank()) {
                add("client_secret", kakaoProperties.clientSecret)
            }
        }

        return try {
            val response = restClientBuilder
                .baseUrl(kakaoProperties.authBaseUrl)
                .build()
                .post()
                .uri("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse::class.java)
                ?: throw ApiException(HttpStatus.BAD_GATEWAY, "카카오 토큰 응답이 비어 있습니다.")

            KakaoAccessToken(response.accessToken)
        } catch (exception: RestClientResponseException) {
            throw ApiException(HttpStatus.BAD_GATEWAY, "카카오 토큰 요청에 실패했습니다.")
        }
    }

    override fun fetchUser(accessToken: String): KakaoUserProfile {
        return try {
            val response = restClientBuilder
                .baseUrl(kakaoProperties.apiBaseUrl)
                .build()
                .get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
                .retrieve()
                .body(KakaoUserResponse::class.java)
                ?: throw ApiException(HttpStatus.BAD_GATEWAY, "카카오 사용자 응답이 비어 있습니다.")

            KakaoUserProfile(
                id = response.id,
                email = response.kakaoAccount?.email,
                nickname = response.kakaoAccount?.profile?.nickname,
            )
        } catch (exception: RestClientResponseException) {
            throw ApiException(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 요청에 실패했습니다.")
        }
    }
}

private data class KakaoTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
)

private data class KakaoUserResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccountResponse?,
)

private data class KakaoAccountResponse(
    val email: String?,
    val profile: KakaoProfileResponse?,
)

private data class KakaoProfileResponse(
    val nickname: String?,
)
