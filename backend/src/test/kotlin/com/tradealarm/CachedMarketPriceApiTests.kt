// KIS 연동이 켜져 있어도 사용자 API가 외부 현재가 API를 직접 호출하지 않는지 검증합니다.
// 현재가는 백그라운드 갱신 스케줄러가 저장하고, HTTP API는 저장된 캐시만 읽어야 합니다.
package com.tradealarm

import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.notification.domain.AlertEventRepository
import com.tradealarm.domain.notification.domain.NotificationChannelRepository
import com.tradealarm.domain.user.domain.UserRepository
import com.tradealarm.domain.watchlist.domain.WatchlistRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest(
    properties = [
        "app.kis.enabled=true",
        "app.kis.base-url=http://127.0.0.1:1",
        "app.market.initial-delay-ms=3600000",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CachedMarketPriceApiTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val alertEventRepository: AlertEventRepository,
    private val notificationChannelRepository: NotificationChannelRepository,
    private val alertRuleRepository: AlertRuleRepository,
    private val watchlistRepository: WatchlistRepository,
    private val priceSnapshotRepository: PriceSnapshotRepository,
    private val userRepository: UserRepository,
) {
    @BeforeEach
    fun cleanUserScopedData() {
        alertEventRepository.deleteAll()
        notificationChannelRepository.deleteAll()
        alertRuleRepository.deleteAll()
        watchlistRepository.deleteAll()
        priceSnapshotRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `관심종목 API는 KIS가 켜져 있어도 저장된 현재가만 사용한다`() {
        val stockId = mockMvc.get("/api/stocks")
            .andExpect {
                status { isOk() }
            }
            .andReturn()
            .response
            .contentAsString
            .substringAfter("\"id\":\"")
            .substringBefore("\"")

        mockMvc.post("/api/watchlist") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"stockId":"$stockId"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.currentPrice") { exists() }
            jsonPath("$.changeRate") { exists() }
        }

        mockMvc.get("/api/watchlist")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].currentPrice") { exists() }
                jsonPath("$[0].changeRate") { exists() }
            }
    }
}
