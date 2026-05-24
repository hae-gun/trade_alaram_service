// KRX/KIND 종목 마스터 파일을 내려받아 내부 종목 DTO로 변환합니다.
package com.tradealarm.domain.stock.infra

import com.tradealarm.domain.stock.domain.Market
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.nio.charset.Charset
import java.net.URLDecoder

@Component
class KrxStockMasterClient(
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder.build()

    fun fetch(source: StockMasterSourceProperties): List<StockMasterItem> {
        val response = restClient.get()
            .uri(source.url)
            .retrieve()
            .toEntity(ByteArray::class.java)
        val charset = response.headers.contentType?.charset ?: Charset.forName("EUC-KR")
        val body = response.body?.toString(charset).orEmpty()

        return parseRows(source.market, body)
    }

    private fun parseRows(market: Market, body: String): List<StockMasterItem> {
        val rows = parseHtmlRows(body).ifEmpty { parseDelimitedRows(body) }
        return rows.mapNotNull { cells ->
            val symbolIndex = cells.indexOfFirst { it.matches(Regex("\\d{6}")) }
            if (symbolIndex < 0) {
                return@mapNotNull null
            }

            val name = cells.take(symbolIndex).lastOrNull { it.isNotBlank() }
                ?: cells.firstOrNull { it.isNotBlank() && !it.matches(Regex("\\d{6}")) }
                ?: return@mapNotNull null

            StockMasterItem(
                market = market,
                symbol = cells[symbolIndex],
                name = name,
            )
        }
    }

    private fun parseHtmlRows(body: String): List<List<String>> {
        val rowRegex = Regex("<tr[^>]*>(.*?)</tr>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val cellRegex = Regex("<t[dh][^>]*>(.*?)</t[dh]>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        return rowRegex.findAll(body)
            .map { row ->
                cellRegex.findAll(row.groupValues[1])
                    .map { cell -> cell.groupValues[1].stripHtml() }
                    .filter { it.isNotBlank() }
                    .toList()
            }
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun parseDelimitedRows(body: String): List<List<String>> {
        return body.lineSequence()
            .map { line ->
                line.split('\t', ',')
                    .map { it.trim().trim('"') }
                    .filter { it.isNotBlank() }
            }
            .filter { it.isNotEmpty() }
            .toList()
    }

    private fun String.stripHtml(): String {
        return replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .let { text -> runCatching { URLDecoder.decode(text, Charsets.UTF_8) }.getOrDefault(text) }
            .trim()
    }
}

data class StockMasterItem(
    val market: Market,
    val symbol: String,
    val name: String,
)
