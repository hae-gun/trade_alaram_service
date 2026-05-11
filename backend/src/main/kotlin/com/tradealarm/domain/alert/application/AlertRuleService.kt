// 알림 조건 생성/조회/토글/삭제 유스케이스를 담당합니다.
// 조건 타입별 필수 입력값을 검증해 잘못된 규칙이 저장되지 않게 합니다.
package com.tradealarm.domain.alert.application

import com.tradealarm.domain.alert.domain.AlertRule
import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.alert.domain.AlertType
import com.tradealarm.domain.alert.domain.RepeatPolicy
import com.tradealarm.domain.stock.application.StockService
import com.tradealarm.domain.user.application.DemoUserService
import com.tradealarm.global.exception.ApiException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class AlertRuleService(
    private val demoUserService: DemoUserService,
    private val stockService: StockService,
    private val alertRuleRepository: AlertRuleRepository,
) {
    @Transactional(readOnly = true)
    fun getMyRules(): List<AlertRule> {
        val user = demoUserService.getOrCreateDemoUser()
        return alertRuleRepository.findAllByUserAndDeletedIsFalseOrderByCreatedAtDesc(user)
    }

    @Transactional
    fun create(
        stockId: UUID,
        type: AlertType,
        targetPrice: BigDecimal?,
        changeRate: BigDecimal?,
        repeatPolicy: RepeatPolicy,
    ): AlertRule {
        if ((type == AlertType.ABOVE_PRICE || type == AlertType.BELOW_PRICE) && targetPrice == null) {
            throw ApiException(HttpStatus.BAD_REQUEST, "targetPrice is required for price alerts.")
        }
        if ((type == AlertType.UP_RATE || type == AlertType.DOWN_RATE) && changeRate == null) {
            throw ApiException(HttpStatus.BAD_REQUEST, "changeRate is required for rate alerts.")
        }

        val user = demoUserService.getOrCreateDemoUser()
        val stock = stockService.get(stockId)
        return alertRuleRepository.save(
            AlertRule(
                user = user,
                stock = stock,
                type = type,
                targetPrice = targetPrice,
                changeRate = changeRate,
                repeatPolicy = repeatPolicy,
            ),
        )
    }

    @Transactional
    fun toggle(ruleId: UUID, enabled: Boolean): AlertRule {
        val rule = alertRuleRepository.findById(ruleId)
            .orElseThrow { ApiException(HttpStatus.NOT_FOUND, "Alert rule not found.") }
        rule.enabled = enabled
        rule.stock.symbol
        return rule
    }

    @Transactional
    fun delete(ruleId: UUID) {
        val rule = alertRuleRepository.findById(ruleId)
            .orElseThrow { ApiException(HttpStatus.NOT_FOUND, "Alert rule not found.") }
        rule.enabled = false
        rule.deleted = true
    }
}
