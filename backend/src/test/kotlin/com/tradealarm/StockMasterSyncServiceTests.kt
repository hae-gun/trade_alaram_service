// 종목 마스터 동기화 시 거래종목코드 기준에서 제외된 기존 종목을 검색 대상에서 제거하는지 검증합니다.
package com.tradealarm

import com.tradealarm.domain.stock.application.StockMasterSyncService
import com.tradealarm.domain.stock.domain.Market
import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.stock.domain.StockRepository
import com.tradealarm.domain.stock.infra.KrxStockMasterClient
import com.tradealarm.domain.stock.infra.StockMasterItem
import com.tradealarm.domain.stock.infra.StockMasterProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest(
    properties = [
        "app.stock-master.enabled=true",
        "app.stock-master.sync-on-startup=false",
    ],
)
@ActiveProfiles("test")
@Transactional
class StockMasterSyncServiceTests @Autowired constructor(
    private val stockMasterSyncService: StockMasterSyncService,
    private val stockRepository: StockRepository,
) {
    @MockBean
    private lateinit var krxStockMasterClient: KrxStockMasterClient

    @BeforeEach
    fun cleanStocks() {
        stockRepository.deleteAll()
        Mockito.reset(krxStockMasterClient)
    }

    @Test
    fun `거래종목코드 마스터에 없는 기존 종목은 비활성화한다`() {
        stockRepository.save(
            Stock(
                market = Market.KOSPI,
                symbol = "999999",
                name = "기존종목",
                enabled = true,
                updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
            ),
        )

        val masterItems = listOf(
            StockMasterItem(Market.KOSPI, "005930", "삼성전자"),
            StockMasterItem(Market.KOSPI, "005935", "삼성전자우"),
        )
        StockMasterProperties().sources.forEach { source ->
            Mockito.doReturn(masterItems).`when`(krxStockMasterClient).fetch(source)
        }

        val result = stockMasterSyncService.sync()
        val stocks = stockRepository.findAll().associateBy { it.symbol }

        assertEquals(2, result.created)
        assertEquals(1, result.disabled)
        assertTrue(stocks.getValue("005930").enabled)
        assertTrue(stocks.getValue("005935").enabled)
        assertFalse(stocks.getValue("999999").enabled)
    }
}
