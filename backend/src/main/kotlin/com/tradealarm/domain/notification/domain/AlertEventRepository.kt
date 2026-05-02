// 알림 발송 이력 JPA 저장소입니다.
// 사용자 대시보드에서 최근 발송 이벤트를 조회할 때 사용합니다.
package com.tradealarm.domain.notification.domain

import com.tradealarm.domain.user.domain.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertEventRepository : JpaRepository<AlertEvent, UUID> {
    @EntityGraph(attributePaths = ["stock"])
    fun findTop50ByUserOrderBySentAtDesc(user: User): List<AlertEvent>
}
