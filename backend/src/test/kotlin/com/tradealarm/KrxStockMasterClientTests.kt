// KRX/KIND 종목 마스터 응답 파싱을 검증합니다.
package com.tradealarm

import com.tradealarm.domain.stock.domain.Market
import com.tradealarm.domain.stock.infra.KrxStockMasterClient
import com.tradealarm.domain.stock.infra.StockMasterSourceProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.nio.charset.StandardCharsets

class KrxStockMasterClientTests {
    @Test
    fun `KIND HTML 종목 마스터에서 종목명과 코드를 추출한다`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KrxStockMasterClient(builder)
        val url = "https://example.com/kospi.xls"

        server.expect(requestTo(url)).andRespond(
            withSuccess(
                """
                    <html>
                      <body>
                        <table>
                          <tr><th>회사명</th><th>종목코드</th><th>업종</th></tr>
                          <tr><td>삼성전자</td><td>005930</td><td>반도체</td></tr>
                          <tr><td>SK하이닉스</td><td>000660</td><td>반도체</td></tr>
                        </table>
                      </body>
                    </html>
                """.trimIndent(),
                MediaType("text", "html", StandardCharsets.UTF_8),
            ),
        )

        val items = client.fetch(StockMasterSourceProperties(Market.KOSPI, url))

        assertEquals(2, items.size)
        assertEquals("005930", items[0].symbol)
        assertEquals("삼성전자", items[0].name)
        assertEquals(Market.KOSPI, items[0].market)
        assertEquals("000660", items[1].symbol)
        server.verify()
    }
}
