// API 응답을 일관된 형태로 감싸기 위한 공통 DTO입니다.
// 현재는 단순 data wrapper이며, 추후 traceId나 pagination metadata를 추가할 수 있습니다.
package com.tradealarm.global.common

data class ApiResponse<T>(
    val data: T,
)
