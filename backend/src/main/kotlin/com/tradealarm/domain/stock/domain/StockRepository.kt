// 주식 종목 마스터의 JPA 저장소입니다.
// 종목 코드 단건 조회와 종목명/코드 검색을 지원합니다.
package com.tradealarm.domain.stock.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import java.util.Optional
import java.util.UUID

interface StockRepository : JpaRepository<Stock, UUID> {
    fun findBySymbol(symbol: String): Optional<Stock>
    fun findTop50ByEnabledIsTrueOrderBySymbolAsc(): List<Stock>
    fun findByEnabledIsTrueAndNameContainingIgnoreCaseOrEnabledIsTrueAndSymbolContainingIgnoreCase(
        name: String,
        symbol: String,
        pageable: Pageable,
    ): List<Stock>
}
