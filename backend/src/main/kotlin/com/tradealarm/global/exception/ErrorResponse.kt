// 클라이언트에 내려주는 공통 에러 응답 모델입니다.
// 상태 코드, 메시지, 발생 시각을 포함해 프론트엔드에서 일관되게 처리할 수 있게 합니다.
package com.tradealarm.global.exception

import java.time.Instant

data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Instant = Instant.now(),
)
