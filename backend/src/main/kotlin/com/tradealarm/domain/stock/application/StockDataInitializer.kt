// 개발용 샘플 종목 데이터를 초기화합니다.
// 한국투자증권 종목 마스터 연동 전에도 화면과 API를 바로 검증할 수 있게 합니다.
package com.tradealarm.domain.stock.application

import com.tradealarm.domain.stock.domain.Market
import com.tradealarm.domain.stock.domain.Stock
import com.tradealarm.domain.stock.domain.StockRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class StockDataInitializer(
    private val stockRepository: StockRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (stockRepository.count() > 0) return

        stockRepository.saveAll(
            listOf(
                Stock(market = Market.KOSPI, symbol = "005930", name = "삼성전자"),
                Stock(market = Market.KOSPI, symbol = "000660", name = "SK하이닉스"),
                Stock(market = Market.KOSPI, symbol = "035420", name = "NAVER"),
                Stock(market = Market.KOSPI, symbol = "035720", name = "카카오"),
                Stock(market = Market.KOSPI, symbol = "005380", name = "현대차"),
                Stock(market = Market.KOSDAQ, symbol = "247540", name = "에코프로비엠"),
                Stock(market = Market.KOSDAQ, symbol = "086520", name = "에코프로"),
            ),
        )
    }
}
