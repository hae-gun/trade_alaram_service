// 사용자 관련 HTTP API입니다.
// 현재는 데모 로그인 상태를 가정하고 /me 응답으로 현재 사용자 정보를 제공합니다.
package com.tradealarm.domain.user.api

import com.tradealarm.domain.user.application.DemoUserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class UserResponse(
    val id: String,
    val email: String,
    val nickname: String,
)

@RestController
@RequestMapping("/api/users")
class UserController(
    private val demoUserService: DemoUserService,
) {
    @GetMapping("/me")
    fun me(): UserResponse {
        val user = demoUserService.getOrCreateDemoUser()
        return UserResponse(
            id = user.id.toString(),
            email = user.email,
            nickname = user.nickname,
        )
    }
}
