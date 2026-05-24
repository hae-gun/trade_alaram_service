// 브라우저 WebSocket 연결을 관리하고 서버 이벤트를 JSON 메시지로 브로드캐스트합니다.
package com.tradealarm.global.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Component
class RealtimeWebSocketHandler(
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions.remove(session.id)
    }

    fun broadcast(event: RealtimeEvent) {
        val message = TextMessage(objectMapper.writeValueAsString(event))
        sessions.values.forEach { session ->
            if (!session.isOpen) {
                sessions.remove(session.id)
                return@forEach
            }

            runCatching {
                synchronized(session) {
                    session.sendMessage(message)
                }
            }.onFailure { error ->
                sessions.remove(session.id)
                log.debug("WebSocket 이벤트 전송 실패: sessionId={}, reason={}", session.id, error.message)
            }
        }
    }
}

data class RealtimeEvent(
    val type: RealtimeEventType,
    val payload: Any,
)

enum class RealtimeEventType {
    PRICE_UPDATED,
    ALERT_EVENT_CREATED,
}
