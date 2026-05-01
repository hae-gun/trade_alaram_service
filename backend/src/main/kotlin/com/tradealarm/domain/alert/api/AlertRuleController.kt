// 알림 조건 HTTP API입니다.
// 프론트엔드의 알림 조건 패널에서 규칙 목록과 생성/수정/삭제 요청을 처리합니다.
package com.tradealarm.domain.alert.api

import com.tradealarm.domain.alert.application.AlertRuleService
import com.tradealarm.domain.alert.domain.AlertRule
import com.tradealarm.domain.alert.domain.AlertType
import com.tradealarm.domain.alert.domain.RepeatPolicy
import com.tradealarm.domain.stock.api.StockResponse
import com.tradealarm.domain.stock.api.toResponse
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateAlertRuleRequest(
    @field:NotNull(message = "stockId is required.")
    val stockId: UUID?,
    @field:NotNull(message = "type is required.")
    val type: AlertType?,
    val targetPrice: BigDecimal?,
    val changeRate: BigDecimal?,
    val repeatPolicy: RepeatPolicy = RepeatPolicy.ONCE,
)

data class ToggleAlertRuleRequest(
    val enabled: Boolean,
)

data class AlertRuleResponse(
    val id: String,
    val stock: StockResponse,
    val type: AlertType,
    val targetPrice: BigDecimal?,
    val changeRate: BigDecimal?,
    val enabled: Boolean,
    val repeatPolicy: RepeatPolicy,
    val lastTriggeredAt: Instant?,
    val createdAt: Instant,
)

@RestController
@RequestMapping("/api/alerts")
class AlertRuleController(
    private val alertRuleService: AlertRuleService,
) {
    @GetMapping
    fun list(): List<AlertRuleResponse> {
        return alertRuleService.getMyRules().map { it.toResponse() }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateAlertRuleRequest): AlertRuleResponse {
        return alertRuleService.create(
            stockId = request.stockId!!,
            type = request.type!!,
            targetPrice = request.targetPrice,
            changeRate = request.changeRate,
            repeatPolicy = request.repeatPolicy,
        ).toResponse()
    }

    @PatchMapping("/{ruleId}")
    fun toggle(
        @PathVariable ruleId: UUID,
        @RequestBody request: ToggleAlertRuleRequest,
    ): AlertRuleResponse {
        return alertRuleService.toggle(ruleId, request.enabled).toResponse()
    }

    @DeleteMapping("/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable ruleId: UUID) {
        alertRuleService.delete(ruleId)
    }
}

fun AlertRule.toResponse(): AlertRuleResponse {
    return AlertRuleResponse(
        id = id.toString(),
        stock = stock.toResponse(),
        type = type,
        targetPrice = targetPrice,
        changeRate = changeRate,
        enabled = enabled,
        repeatPolicy = repeatPolicy,
        lastTriggeredAt = lastTriggeredAt,
        createdAt = createdAt,
    )
}
