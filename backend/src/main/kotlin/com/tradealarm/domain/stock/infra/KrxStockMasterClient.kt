// KIS 거래종목코드 마스터 파일을 내려받아 내부 종목 DTO로 변환합니다.
package com.tradealarm.domain.stock.infra

import com.tradealarm.domain.stock.domain.Market
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.net.URLDecoder
import java.util.zip.ZipInputStream

@Component
class KrxStockMasterClient(
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder.build()
    private val masterCharset = Charset.forName("CP949")

    fun fetch(source: StockMasterSourceProperties): List<StockMasterItem> {
        val response = restClient.get()
            .uri(source.url)
            .retrieve()
            .toEntity(ByteArray::class.java)
        val bytes = response.body ?: ByteArray(0)
        if (source.url.endsWith(".zip", ignoreCase = true)) {
            return parseKisZipRows(source.market, bytes)
        }

        val charset = response.headers.contentType?.charset ?: Charset.forName("EUC-KR")
        val body = bytes.toString(charset)

        return parseRows(source.market, body)
    }

    private fun parseKisZipRows(market: Market, bytes: ByteArray): List<StockMasterItem> {
        return ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            generateSequence { zip.nextEntry }
                .filterNot { it.isDirectory }
                .flatMap {
                    val content = zip.readBytes().toString(masterCharset)
                    parseKisFixedRows(market, content).asSequence()
                }
                .toList()
        }
    }

    private fun parseKisFixedRows(market: Market, body: String): List<StockMasterItem> {
        return body.lineSequence()
            .mapNotNull { line ->
                if (line.length < KIS_NAME_START_INDEX) {
                    return@mapNotNull null
                }

                val symbol = line.substring(0, KIS_SYMBOL_LENGTH).trim()
                if (!symbol.matches(Regex("\\d{6}"))) {
                    return@mapNotNull null
                }

                val nameEndIndex = minOf(line.length, KIS_NAME_START_INDEX + KIS_NAME_LENGTH)
                val name = line.substring(KIS_NAME_START_INDEX, nameEndIndex).trim()
                    .takeIf { it.isValidStockName() }
                    ?: return@mapNotNull null

                StockMasterItem(
                    market = market,
                    symbol = symbol,
                    name = name,
                )
            }
            .toList()
    }

    private fun parseRows(market: Market, body: String): List<StockMasterItem> {
        val rows = parseHtmlRows(body).ifEmpty { parseDelimitedRows(body) }
        val header = rows.firstOrNull().orEmpty()
        val symbolIndex = header.findColumnIndex("종목코드", "단축코드", "표준코드")
        val nameIndex = header.findColumnIndex("회사명", "종목명", "한글 종목명", "한글종목명")
        val dataRows = if (symbolIndex >= 0 && nameIndex >= 0) rows.drop(1) else rows

        return dataRows.mapNotNull { cells ->
            val symbol = cells.getOrNull(symbolIndex)
                ?.takeIf { it.matches(Regex("\\d{6}")) }
                ?: cells.firstOrNull { it.matches(Regex("\\d{6}")) }
                ?: return@mapNotNull null
            val name = cells.getOrNull(nameIndex)
                ?.takeIf { it.isValidStockName() }
                ?: inferName(cells, symbol)
                ?: return@mapNotNull null

            StockMasterItem(
                market = market,
                symbol = symbol,
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

    private fun List<String>.findColumnIndex(vararg candidates: String): Int {
        return indexOfFirst { header ->
            val normalizedHeader = header.normalizeHeader()
            candidates.any { candidate -> normalizedHeader == candidate.normalizeHeader() }
        }
    }

    private fun String.normalizeHeader(): String {
        return replace(Regex("\\s+"), "")
            .replace("'", "")
            .replace("\"", "")
            .trim()
    }

    private fun inferName(cells: List<String>, symbol: String): String? {
        val symbolIndex = cells.indexOf(symbol)
        val candidates = cells.drop(symbolIndex + 1) + cells.take(symbolIndex)
        return candidates.firstOrNull { it.isValidStockName() }
    }

    private fun String.isValidStockName(): Boolean {
        return isNotBlank() &&
            !matches(Regex("\\d{6}")) &&
            this !in setOf("유가", "코스닥", "코넥스", "KOSPI", "KOSDAQ", "KONEX")
    }

    companion object {
        private const val KIS_SYMBOL_LENGTH = 6
        private const val KIS_NAME_START_INDEX = 21
        private const val KIS_NAME_LENGTH = 40
    }
}

data class StockMasterItem(
    val market: Market,
    val symbol: String,
    val name: String,
)
