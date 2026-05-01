// 애플리케이션 전역 예외를 HTTP 응답으로 변환하는 핸들러입니다.
// 도메인 예외와 요청 검증 실패를 우선 처리합니다.
package com.tradealarm.global.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ApiException::class)
    fun handleApiException(exception: ApiException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(exception.status)
            .body(ErrorResponse(exception.status.value(), exception.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = exception.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "Invalid request."
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), message))
    }
}
