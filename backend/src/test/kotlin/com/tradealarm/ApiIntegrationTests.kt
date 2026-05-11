// 백엔드 MVP API의 HTTP 계약을 검증하는 통합 테스트입니다.
// admin 사용자 기준으로 종목, 관심종목, 알림 조건, 알림 채널 API가 H2 DB와 연결되는지 확인합니다.
package com.tradealarm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.market.domain.PriceSnapshotRepository
import com.tradealarm.domain.notification.domain.AlertEvent
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
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
    fun `admin 사용자 정보를 조회한다`() {
        mockMvc.get("/api/users/me")
            .andExpect {
                status { isOk() }
                jsonPath("$.email") { value("admin@tradealarm.local") }
                jsonPath("$.nickname") { value("admin") }
            }
    }

    @Test
    fun `종목 목록과 검색 결과를 조회한다`() {
        mockMvc.get("/api/stocks")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(7))
                jsonPath("$[0].symbol") { value("005930") }
                jsonPath("$[0].name") { value("삼성전자") }
            }

        mockMvc.get("/api/stocks") {
            param("query", "NAVER")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].symbol") { value("035420") }
            jsonPath("$[0].name") { value("NAVER") }
        }
    }

    @Test
    fun `관심종목을 추가하고 목록 조회 후 삭제한다`() {
        val stockId = firstStockId()

        mockMvc.post("/api/watchlist") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"stockId":"$stockId"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.stock.id") { value(stockId) }
            jsonPath("$.stock.symbol") { value("005930") }
            jsonPath("$.currentPrice") { exists() }
            jsonPath("$.changeRate") { exists() }
        }

        mockMvc.get("/api/watchlist")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].stock.id") { value(stockId) }
            }

        mockMvc.delete("/api/watchlist/$stockId")
            .andExpect {
                status { isNoContent() }
            }

        mockMvc.get("/api/watchlist")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(0))
            }
    }

    @Test
    fun `알림 조건을 생성하고 비활성화 후 삭제한다`() {
        val stockId = firstStockId()

        val createdRule = mockMvc.post("/api/alerts") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "stockId": "$stockId",
                  "type": "ABOVE_PRICE",
                  "targetPrice": 75000,
                  "repeatPolicy": "ONCE"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.stock.id") { value(stockId) }
            jsonPath("$.type") { value("ABOVE_PRICE") }
            jsonPath("$.enabled") { value(true) }
        }.andReturn().response.contentAsString.contentAsJson()

        val ruleId = createdRule["id"].asText()

        mockMvc.get("/api/alerts")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].id") { value(ruleId) }
            }

        mockMvc.patch("/api/alerts/$ruleId") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"enabled":false}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.enabled") { value(false) }
        }

        mockMvc.delete("/api/alerts/$ruleId")
            .andExpect {
                status { isNoContent() }
            }

        mockMvc.get("/api/alerts")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(0))
            }
    }

    @Test
    fun `발송 이력이 있는 알림 조건도 삭제하면 목록에서 제외하고 이력은 보존한다`() {
        val stockId = firstStockId()

        val createdRule = mockMvc.post("/api/alerts") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "stockId": "$stockId",
                  "type": "ABOVE_PRICE",
                  "targetPrice": 100,
                  "repeatPolicy": "COOLDOWN"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }.andReturn().response.contentAsString.contentAsJson()

        val ruleId = createdRule["id"].asText()
        val rule = alertRuleRepository.findAllByEnabledIsTrueAndDeletedIsFalse()
            .first { it.id.toString() == ruleId }
        alertEventRepository.save(
            AlertEvent(
                user = rule.user,
                alertRule = rule,
                stock = rule.stock,
                triggerPrice = rule.targetPrice!!,
                triggerChangeRate = java.math.BigDecimal.ZERO,
                message = "테스트 알림",
            ),
        )

        mockMvc.delete("/api/alerts/$ruleId")
            .andExpect {
                status { isNoContent() }
            }

        mockMvc.get("/api/alerts")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(0))
            }

        mockMvc.get("/api/notifications/events")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].message") { value("테스트 알림") }
            }
    }

    @Test
    fun `이메일 알림 채널을 등록하고 목록을 조회한다`() {
        mockMvc.post("/api/notifications/channels/email") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"admin@tradealarm.local"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.type") { value("EMAIL") }
            jsonPath("$.destination") { value("admin@tradealarm.local") }
            jsonPath("$.verified") { value(false) }
        }

        mockMvc.get("/api/notifications/channels")
            .andExpect {
                status { isOk() }
                jsonPath("$", hasSize<Any>(1))
                jsonPath("$[0].destination") { value("admin@tradealarm.local") }
            }
    }

    @Test
    fun `필수값이 빠진 요청은 400을 반환한다`() {
        mockMvc.post("/api/watchlist") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}"
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }

        mockMvc.post("/api/alerts") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"type":"ABOVE_PRICE","targetPrice":75000}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }

    private fun firstStockId(): String {
        val response = mockMvc.get("/api/stocks")
            .andExpect {
                status { isOk() }
            }
            .andReturn()
            .response
            .contentAsString
            .contentAsJson()

        return response[0]["id"].asText()
    }

    private fun String.contentAsJson(): JsonNode = objectMapper.readTree(this)
}
