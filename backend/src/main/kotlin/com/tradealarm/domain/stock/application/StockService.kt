// 종목 검색과 단건 조회 유스케이스를 담당합니다.
// 컨트롤러가 저장소에 직접 접근하지 않도록 도메인 조회 경계를 제공합니다.
package com.tradealarm.domain.stock.application

import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.stock.domain.StockRepository
import com.tradealarm.global.exception.ApiException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class StockService(
    private val stockRepository: StockRepository,
) {
    @Transactional(readOnly = true)
    fun search(query: String?): List<Stock> {
        if (query.isNullOrBlank()) {
            return stockRepository.findAll().filter { it.enabled }.take(20)
        }

        return stockRepository
            .findTop20ByEnabledIsTrueAndNameContainingIgnoreCaseOrEnabledIsTrueAndSymbolContainingIgnoreCase(
                query.trim(),
                query.trim(),
            )
    }

    @Transactional(readOnly = true)
    fun get(stockId: UUID): Stock {
        return stockRepository.findById(stockId)
            .orElseThrow { ApiException(HttpStatus.NOT_FOUND, "Stock not found.") }
    }
}
