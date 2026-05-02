// 알림 조건 JPA 저장소입니다.
// 사용자별 조건 목록과 스케줄러가 평가할 활성 조건 목록을 조회합니다.
package com.tradealarm.domain.alert.domain

import com.tradealarm.domain.user.domain.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertRuleRepository : JpaRepository<AlertRule, UUID> {
    @EntityGraph(attributePaths = ["stock"])
    fun findAllByUserOrderByCreatedAtDesc(user: User): List<AlertRule>

    @EntityGraph(attributePaths = ["user", "stock"])
    fun findAllByEnabledIsTrue(): List<AlertRule>
}
