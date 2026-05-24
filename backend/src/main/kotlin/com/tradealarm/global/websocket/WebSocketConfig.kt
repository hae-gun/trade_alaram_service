// WebSocket 엔드포인트를 등록합니다.
package com.tradealarm.global.websocket

import com.tradealarm.global.config.CorsProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val realtimeWebSocketHandler: RealtimeWebSocketHandler,
    private val corsProperties: CorsProperties,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(realtimeWebSocketHandler, "/ws/stream")
            .setAllowedOrigins(*corsProperties.allowedOrigins.toTypedArray())
    }
}
