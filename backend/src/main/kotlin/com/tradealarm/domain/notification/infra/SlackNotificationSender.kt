// Slack Incoming Webhook을 통해 알림 메시지를 전송합니다.
// Webhook URL은 환경변수 기반 설정에서만 읽고 로그에 출력하지 않습니다.
package com.tradealarm.domain.notification.infra

import com.tradealarm.domain.notification.domain.NotificationStatus
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@ConfigurationProperties(prefix = "app.slack")
data class SlackProperties(
    val enabled: Boolean = false,
    val webhookUrl: String = "",
) {
    fun isConfigured(): Boolean = enabled && webhookUrl.isNotBlank()
}

@Component
class SlackNotificationSender(
    private val slackProperties: SlackProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = restClientBuilder.build()

    fun send(message: String, mention: String?): NotificationStatus {
        if (!slackProperties.enabled) {
            return NotificationStatus.PENDING
        }
        if (!slackProperties.isConfigured()) {
            log.warn("Slack 알림 전송 실패: webhook URL이 설정되지 않았습니다.")
            return NotificationStatus.FAILED
        }

        val text = listOfNotNull(formatMention(mention), message).joinToString("\n")
        return runCatching {
            restClient
                .post()
                .uri(slackProperties.webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(SlackWebhookRequest(text = text))
                .retrieve()
                .toBodilessEntity()
            NotificationStatus.SENT
        }.getOrElse { error ->
            log.warn("Slack 알림 전송 실패: {}", error.message)
            NotificationStatus.FAILED
        }
    }

    private fun formatMention(mention: String?): String? {
        val value = mention?.trim().orEmpty()
        if (value.isBlank()) return null
        if (value.startsWith("<@") && value.endsWith(">")) return value
        if (value.startsWith("U") || value.startsWith("W")) return "<@$value>"
        return value
    }
}

private data class SlackWebhookRequest(
    val text: String,
)
