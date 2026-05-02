// 한국투자증권 OAuth 접근 토큰을 발급하고 메모리에 캐시합니다.
// 토큰은 만료 1분 전까지 재사용하며, 앱 키/시크릿은 환경변수 기반 설정에서만 읽습니다.
package com.tradealarm.domain.market.infra

import com.fasterxml.jackson.annotation.JsonProperty
import com.tradealarm.global.exception.ApiException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Instant

@Component
class KisTokenProvider(
    private val kisProperties: KisProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder
        .baseUrl(kisProperties.baseUrl)
        .build()

    @Volatile
    private var cachedToken: CachedToken? = null

    fun getAccessToken(): String {
        cachedToken?.let { token ->
            if (token.expiresAt.isAfter(Instant.now().plusSeconds(60))) {
                return token.value
            }
        }

        if (!kisProperties.isConfigured()) {
            throw ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "KIS API 키가 설정되지 않았습니다.")
        }

        val response = restClient.post()
            .uri("/oauth2/tokenP")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                KisTokenRequest(
                    appKey = kisProperties.appKey,
                    appSecret = kisProperties.appSecret,
                ),
            )
            .retrieve()
            .body(KisTokenResponse::class.java)
            ?: throw ApiException(HttpStatus.BAD_GATEWAY, "KIS 토큰 응답이 비어 있습니다.")

        val token = CachedToken(
            value = response.accessToken,
            expiresAt = Instant.now().plusSeconds(response.expiresIn.coerceAtLeast(300)),
        )
        cachedToken = token
        return token.value
    }

    private data class CachedToken(
        val value: String,
        val expiresAt: Instant,
    )
}

data class KisTokenRequest(
    @JsonProperty("grant_type")
    val grantType: String = "client_credentials",
    @JsonProperty("appkey")
    val appKey: String,
    @JsonProperty("appsecret")
    val appSecret: String,
)

data class KisTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Long = 86_400,
)
