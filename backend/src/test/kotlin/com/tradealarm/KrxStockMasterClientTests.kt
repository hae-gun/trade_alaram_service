// KIS/KIND 종목 마스터 응답 파싱을 검증합니다.
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
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class KrxStockMasterClientTests {
    @Test
    fun `KIS 거래종목코드 마스터에서 우선주 포함 종목명과 코드를 추출한다`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KrxStockMasterClient(builder)
        val url = "https://example.com/kospi_code.mst.zip"

        server.expect(requestTo(url)).andRespond(
            withSuccess(
                zipBytes(
                    entryName = "kospi_code.mst",
                    lines = listOf(
                        kisMasterLineBytes("005930", "KR7005930003", "삼성전자"),
                        kisMasterLineBytes("005935", "KR7005931001", "삼성전자우"),
                    ),
                ),
                MediaType.APPLICATION_OCTET_STREAM,
            ),
        )

        val items = client.fetch(StockMasterSourceProperties(Market.KOSPI, url))

        assertEquals(2, items.size)
        assertEquals("005930", items[0].symbol)
        assertEquals("삼성전자", items[0].name)
        assertEquals("005935", items[1].symbol)
        assertEquals("삼성전자우", items[1].name)
        server.verify()
    }

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

    @Test
    fun `시장구분이 종목코드 앞에 있어도 헤더 기준으로 종목명을 추출한다`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KrxStockMasterClient(builder)
        val url = "https://example.com/all-market.xls"

        server.expect(requestTo(url)).andRespond(
            withSuccess(
                """
                    <html>
                      <body>
                        <table>
                          <tr><th>시장구분</th><th>종목코드</th><th>종목명</th><th>업종</th></tr>
                          <tr><td>유가</td><td>005930</td><td>삼성전자</td><td>반도체</td></tr>
                          <tr><td>코스닥</td><td>247540</td><td>에코프로비엠</td><td>전기제품</td></tr>
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
        assertEquals("247540", items[1].symbol)
        assertEquals("에코프로비엠", items[1].name)
        server.verify()
    }

    @Test
    fun `KIND 실제 컬럼 순서에서 회사명을 종목명으로 추출한다`() {
        val builder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KrxStockMasterClient(builder)
        val url = "https://example.com/stock-market.xls"

        server.expect(requestTo(url)).andRespond(
            withSuccess(
                """
                    <html>
                      <body>
                        <table>
                          <tr><th>회사명</th><th>시장구분</th><th>종목코드</th><th>업종</th></tr>
                          <tr><td>동화약품</td><td>유가</td><td>000020</td><td>의약품 제조업</td></tr>
                          <tr><td>KR모터스</td><td>유가</td><td>000040</td><td>그 외 기타 운송장비 제조업</td></tr>
                        </table>
                      </body>
                    </html>
                """.trimIndent(),
                MediaType("text", "html", StandardCharsets.UTF_8),
            ),
        )

        val items = client.fetch(StockMasterSourceProperties(Market.KOSPI, url))

        assertEquals(2, items.size)
        assertEquals("000020", items[0].symbol)
        assertEquals("동화약품", items[0].name)
        assertEquals("000040", items[1].symbol)
        assertEquals("KR모터스", items[1].name)
        server.verify()
    }

    private fun kisMasterLineBytes(symbol: String, standardCode: String, name: String): ByteArray {
        val prefix = (symbol + "   " + standardCode).toByteArray(StandardCharsets.US_ASCII)
        val nameArea = ByteArray(40) { ' '.code.toByte() }
        val nameBytes = name.toByteArray(Charset.forName("CP949"))
        nameBytes.copyInto(nameArea)
        val suffix = "ST10".toByteArray(StandardCharsets.US_ASCII)
        return prefix + nameArea + suffix
    }

    private fun zipBytes(entryName: String, lines: List<ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(entryName))
            lines.forEachIndexed { index, line ->
                if (index > 0) {
                    zip.write('\n'.code)
                }
                zip.write(line)
            }
            zip.closeEntry()
        }
        return output.toByteArray()
    }
}
