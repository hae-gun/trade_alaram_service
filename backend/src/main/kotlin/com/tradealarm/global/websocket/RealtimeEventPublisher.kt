// 도메인 이벤트를 WebSocket 메시지로 변환해 프론트엔드에 전달합니다.
package com.tradealarm.global.websocket

import com.tradealarm.domain.market.domain.PriceSnapshot
import com.tradealarm.domain.notification.api.toResponse
import com.tradealarm.domain.notification.domain.AlertEvent
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.Instant

@Component
class RealtimeEventPublisher(
    private val webSocketHandler: RealtimeWebSocketHandler,
) {
    fun publishPriceUpdated(snapshot: PriceSnapshot) {
        webSocketHandler.broadcast(
            RealtimeEvent(
                type = RealtimeEventType.PRICE_UPDATED,
                payload = PriceUpdatedPayload(
                    stockId = snapshot.stock.id.toString(),
                    symbol = snapshot.stock.symbol,
                    price = snapshot.price,
                    changeRate = snapshot.changeRate,
                    capturedAt = snapshot.capturedAt,
                ),
            ),
        )
    }

    fun publishAlertEventCreated(event: AlertEvent) {
        webSocketHandler.broadcast(
            RealtimeEvent(
                type = RealtimeEventType.ALERT_EVENT_CREATED,
                payload = event.toResponse(),
            ),
        )
    }
}

data class PriceUpdatedPayload(
    val stockId: String,
    val symbol: String,
    val price: BigDecimal,
    val changeRate: BigDecimal,
    val capturedAt: Instant,
)
