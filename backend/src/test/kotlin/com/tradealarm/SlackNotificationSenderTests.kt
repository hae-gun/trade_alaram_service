// Slack Webhook 송신 계약을 검증합니다.
// 실제 Slack URL을 호출하지 않고 MockRestServiceServer로 요청 본문만 확인합니다.
package com.tradealarm

import com.tradealarm.domain.notification.domain.NotificationStatus
import com.tradealarm.domain.notification.infra.SlackNotificationSender
import com.tradealarm.domain.notification.infra.SlackProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class SlackNotificationSenderTests {
    @Test
    fun `Slack이 비활성화되어 있으면 전송하지 않고 대기 상태를 반환한다`() {
        val sender = SlackNotificationSender(
            SlackProperties(enabled = false, webhookUrl = ""),
            RestClient.builder(),
        )

        val status = sender.send("테스트 메시지", null)

        assertThat(status).isEqualTo(NotificationStatus.PENDING)
    }

    @Test
    fun `Slack Webhook으로 mention과 메시지를 전송한다`() {
        val restClientBuilder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(restClientBuilder).build()
        val sender = SlackNotificationSender(
            SlackProperties(enabled = true, webhookUrl = "https://hooks.slack.test/services/test"),
            restClientBuilder,
        )

        server.expect(ExpectedCount.once(), requestTo("https://hooks.slack.test/services/test"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.text").value("<@U123456>\n삼성전자 알림"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN))

        val status = sender.send("삼성전자 알림", "U123456")

        assertThat(status).isEqualTo(NotificationStatus.SENT)
        server.verify()
    }
}
