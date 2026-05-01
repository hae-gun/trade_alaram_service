// 알림 발송과 알림 관련 조회 유스케이스를 담당합니다.
// 현재는 실제 외부 발송 전 단계로 발송 이벤트를 DB에 기록하는 mock 발송 역할을 합니다.
package com.tradealarm.domain.notification.application

import com.tradealarm.domain.alert.domain.AlertRule
import com.tradealarm.domain.notification.domain.AlertEvent
import com.tradealarm.domain.notification.domain.AlertEventRepository
import com.tradealarm.domain.notification.domain.NotificationChannel
import com.tradealarm.domain.notification.domain.NotificationChannelRepository
import com.tradealarm.domain.notification.domain.NotificationChannelType
import com.tradealarm.domain.user.application.DemoUserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class NotificationService(
    private val demoUserService: DemoUserService,
    private val alertEventRepository: AlertEventRepository,
    private val notificationChannelRepository: NotificationChannelRepository,
) {
    @Transactional(readOnly = true)
    fun getMyEvents(): List<AlertEvent> {
        val user = demoUserService.getOrCreateDemoUser()
        return alertEventRepository.findTop50ByUserOrderBySentAtDesc(user)
    }

    @Transactional(readOnly = true)
    fun getMyChannels(): List<NotificationChannel> {
        val user = demoUserService.getOrCreateDemoUser()
        return notificationChannelRepository.findAllByUserOrderByTypeAsc(user)
    }

    @Transactional
    fun sendAlert(rule: AlertRule, price: BigDecimal, changeRate: BigDecimal): AlertEvent {
        val message = "${rule.stock.name}(${rule.stock.symbol}) 알림 조건이 충족되었습니다. 현재가 ${price}원, 등락률 ${changeRate}%"
        return alertEventRepository.save(
            AlertEvent(
                user = rule.user,
                alertRule = rule,
                stock = rule.stock,
                triggerPrice = price,
                triggerChangeRate = changeRate,
                message = message,
            ),
        )
    }

    @Transactional
    fun createEmailChannel(destination: String): NotificationChannel {
        val user = demoUserService.getOrCreateDemoUser()
        return notificationChannelRepository.save(
            NotificationChannel(
                user = user,
                type = NotificationChannelType.EMAIL,
                destination = destination,
                verified = false,
            ),
        )
    }
}
