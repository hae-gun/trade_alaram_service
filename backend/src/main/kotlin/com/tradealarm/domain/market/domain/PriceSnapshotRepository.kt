// 가격 스냅샷의 JPA 저장소입니다.
// 종목별 최신 가격을 조회해 polling/mock 시세의 기준으로 사용합니다.
package com.tradealarm.domain.market.domain

import com.tradealarm.domain.stock.domain.Stock
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PriceSnapshotRepository : JpaRepository<PriceSnapshot, UUID> {
    fun findTopByStockOrderByCapturedAtDesc(stock: Stock): Optional<PriceSnapshot>
}
