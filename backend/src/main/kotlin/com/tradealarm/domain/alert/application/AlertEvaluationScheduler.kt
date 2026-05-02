// 활성 알림 조건을 주기적으로 평가하는 스케줄러입니다.
// 조건이 충족되면 알림 이벤트를 기록하고 반복 정책에 따라 비활성화 또는 쿨다운을 적용합니다.
package com.tradealarm.domain.alert.application

import com.tradealarm.domain.alert.domain.AlertRuleRepository
import com.tradealarm.domain.alert.domain.RepeatPolicy
import com.tradealarm.domain.market.application.MarketPriceService
import com.tradealarm.domain.notification.application.NotificationService
import com.tradealarm.global.lock.DistributedLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class AlertEvaluationScheduler(
    private val alertRuleRepository: AlertRuleRepository,
    private val marketPriceService: MarketPriceService,
    private val notificationService: NotificationService,
    private val distributedLock: DistributedLock,
) {
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    fun evaluate() {
        val lockKey = "tradealarm:lock:alert-evaluation"
        if (!distributedLock.acquire(lockKey, Duration.ofSeconds(55))) {
            return
        }

        try {
            alertRuleRepository.findAllByEnabledIsTrue().forEach { rule ->
                if (!canTrigger(rule.lastTriggeredAt, rule.repeatPolicy)) {
                    return@forEach
                }

                val snapshot = marketPriceService.getCurrentPrice(rule.stock)
                if (rule.isTriggered(snapshot.price, snapshot.changeRate)) {
                    notificationService.sendAlert(rule, snapshot.price, snapshot.changeRate)
                    rule.lastTriggeredAt = Instant.now()
                    if (rule.repeatPolicy == RepeatPolicy.ONCE) {
                        rule.enabled = false
                    }
                }
            }
        } finally {
            distributedLock.release(lockKey)
        }
    }

    private fun canTrigger(lastTriggeredAt: Instant?, repeatPolicy: RepeatPolicy): Boolean {
        if (lastTriggeredAt == null) return true
        if (repeatPolicy == RepeatPolicy.ONCE) return false
        return lastTriggeredAt.plus(30, ChronoUnit.MINUTES).isBefore(Instant.now())
    }
}
