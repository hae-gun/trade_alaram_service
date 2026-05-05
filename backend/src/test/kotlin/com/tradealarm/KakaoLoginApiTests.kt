// 카카오 로그인 API의 백엔드 계약을 검증합니다.
// 외부 카카오 API는 mock 처리하고 내부 사용자 생성/조회 흐름만 확인합니다.
package com.tradealarm

import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.auth.infra.KakaoAccessToken
import com.tradealarm.domain.auth.infra.KakaoAuthClient
import com.tradealarm.domain.auth.infra.KakaoUserProfile
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.notification.domain.AlertEventRepository
import com.tradealarm.domain.notification.domain.NotificationChannelRepository
import com.tradealarm.domain.user.domain.AuthProvider
import com.tradealarm.domain.user.domain.UserRepository
import com.tradealarm.domain.watchlist.domain.WatchlistRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class KakaoLoginApiTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val alertEventRepository: AlertEventRepository,
    private val notificationChannelRepository: NotificationChannelRepository,
    private val alertRuleRepository: AlertRuleRepository,
    private val watchlistRepository: WatchlistRepository,
    private val priceSnapshotRepository: PriceSnapshotRepository,
    private val userRepository: UserRepository,
) {
    @MockBean
    private lateinit var kakaoAuthClient: KakaoAuthClient

    @BeforeEach
    fun cleanUserScopedData() {
        Mockito.reset(kakaoAuthClient)
        alertEventRepository.deleteAll()
        notificationChannelRepository.deleteAll()
        alertRuleRepository.deleteAll()
        watchlistRepository.deleteAll()
        priceSnapshotRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `카카오 인가 코드로 신규 사용자를 생성하고 로그인 응답을 반환한다`() {
        mockKakaoUser(
            authorizationCode = "valid-code",
            accessToken = "kakao-access-token",
            user = KakaoUserProfile(
                id = 12345L,
                email = "kakao-user@example.com",
                nickname = "카카오사용자",
            ),
        )

        mockMvc.post("/api/auth/kakao/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"authorizationCode":"valid-code"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.provider") { value("KAKAO") }
            jsonPath("$.isNewUser") { value(true) }
            jsonPath("$.user.email") { value("kakao-user@example.com") }
            jsonPath("$.user.nickname") { value("카카오사용자") }
        }

        val savedUser = userRepository.findByProviderAndProviderUserId(AuthProvider.KAKAO, "12345").orElseThrow()
        assertEquals("kakao-user@example.com", savedUser.email)
    }

    @Test
    fun `이미 가입된 카카오 사용자는 기존 사용자를 반환한다`() {
        mockKakaoUser(
            authorizationCode = "valid-code",
            accessToken = "kakao-access-token",
            user = KakaoUserProfile(
                id = 12345L,
                email = "kakao-user@example.com",
                nickname = "카카오사용자",
            ),
        )

        repeat(2) {
            mockMvc.post("/api/auth/kakao/login") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"authorizationCode":"valid-code"}"""
            }.andExpect {
                status { isOk() }
            }
        }

        val users = userRepository.findAll()
        assertEquals(1, users.size)
        assertEquals(AuthProvider.KAKAO, users.first().provider)
    }

    @Test
    fun `카카오 인가 코드가 없으면 400을 반환한다`() {
        mockMvc.post("/api/auth/kakao/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"authorizationCode":""}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }

    private fun mockKakaoUser(
        authorizationCode: String,
        accessToken: String,
        user: KakaoUserProfile,
    ) {
        Mockito.`when`(
            kakaoAuthClient.requestAccessToken(
                authorizationCode,
                "http://localhost:3000/auth/kakao/callback",
            ),
        ).thenReturn(KakaoAccessToken(accessToken))
        Mockito.`when`(kakaoAuthClient.fetchUser(accessToken)).thenReturn(user)
    }
}
