// 사용자별 알림 수신 채널 JPA 저장소입니다.
// 이메일, 카카오 알림톡, Web Push 채널을 같은 방식으로 조회합니다.
package com.tradealarm.domain.notification.domain

import com.tradealarm.domain.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationChannelRepository : JpaRepository<NotificationChannel, UUID> {
    fun findAllByUserOrderByTypeAsc(user: User): List<NotificationChannel>

    fun findFirstByUserAndTypeAndEnabledIsTrue(user: User, type: NotificationChannelType): NotificationChannel?
}
