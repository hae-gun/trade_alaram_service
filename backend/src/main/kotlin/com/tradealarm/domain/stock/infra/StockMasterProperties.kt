// 전체 종목 마스터 동기화 설정입니다.
// KIS 현재가 API 호출 제한을 피하기 위해 종목 목록은 KIS 거래종목코드 마스터 파일에서 갱신합니다.
package com.tradealarm.domain.stock.infra

import com.tradealarm.domain.stock.domain.Market
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.stock-master")
data class StockMasterProperties(
    val enabled: Boolean = true,
    val syncOnStartup: Boolean = true,
    val requestDelayMs: Long = 500,
    val sources: List<StockMasterSourceProperties> = listOf(
        StockMasterSourceProperties(
            market = Market.KOSPI,
            url = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip",
        ),
        StockMasterSourceProperties(
            market = Market.KOSDAQ,
            url = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip",
        ),
    ),
)

data class StockMasterSourceProperties(
    val market: Market,
    val url: String,
)
