// 서비스 계층에서 의도한 HTTP 상태와 메시지를 함께 던지기 위한 예외 타입입니다.
// 컨트롤러는 GlobalExceptionHandler를 통해 동일한 에러 응답 형식으로 변환됩니다.
package com.tradealarm.global.exception

import org.springframework.http.HttpStatus

class ApiException(
    val status: HttpStatus,
    override val message: String,
) : RuntimeException(message)
