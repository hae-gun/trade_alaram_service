// 알림 이력과 수신 채널 HTTP API입니다.
// 대시보드의 발송 이력 표시와 이메일 채널 등록 흐름에서 사용합니다.
package com.tradealarm.domain.notification.api

import com.tradealarm.domain.notification.application.NotificationService
import com.tradealarm.domain.notification.domain.AlertEvent
import com.tradealarm.domain.notification.domain.NotificationChannel
import com.tradealarm.domain.notification.domain.NotificationChannelType
import com.tradealarm.domain.notification.domain.NotificationStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

data class CreateEmailChannelRequest(
    @field:NotBlank(message = "email is required.")
    @field:Email(message = "email format is invalid.")
    val email: String,
)

data class NotificationChannelResponse(
    val id: String,
    val type: NotificationChannelType,
    val destination: String,
    val verified: Boolean,
    val enabled: Boolean,
)

data class AlertEventResponse(
    val id: String,
    val stockSymbol: String,
    val stockName: String,
    val triggerPrice: BigDecimal,
    val triggerChangeRate: BigDecimal,
    val message: String,
    val status: NotificationStatus,
    val sentAt: Instant,
)

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {
    @GetMapping("/events")
    fun events(): List<AlertEventResponse> {
        return notificationService.getMyEvents().map { it.toResponse() }
    }

    @GetMapping("/channels")
    fun channels(): List<NotificationChannelResponse> {
        return notificationService.getMyChannels().map { it.toResponse() }
    }

    @PostMapping("/channels/email")
    @ResponseStatus(HttpStatus.CREATED)
    fun createEmailChannel(@RequestBody request: CreateEmailChannelRequest): NotificationChannelResponse {
        return notificationService.createEmailChannel(request.email).toResponse()
    }
}

fun AlertEvent.toResponse(): AlertEventResponse {
    return AlertEventResponse(
        id = id.toString(),
        stockSymbol = stock.symbol,
        stockName = stock.name,
        triggerPrice = triggerPrice,
        triggerChangeRate = triggerChangeRate,
        message = message,
        status = status,
        sentAt = sentAt,
    )
}

fun NotificationChannel.toResponse(): NotificationChannelResponse {
    return NotificationChannelResponse(
        id = id.toString(),
        type = type,
        destination = destination,
        verified = verified,
        enabled = enabled,
    )
}
