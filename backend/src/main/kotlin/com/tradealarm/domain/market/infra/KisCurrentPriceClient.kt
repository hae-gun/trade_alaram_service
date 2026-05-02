// 한국투자증권 국내주식 현재가 API 클라이언트입니다.
// /uapi/domestic-stock/v1/quotations/inquire-price 응답을 내부 가격 DTO로 변환합니다.
package com.tradealarm.domain.market.infra

import com.fasterxml.jackson.annotation.JsonProperty
import com.tradealarm.global.exception.ApiException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class KisCurrentPriceClient(
    private val kisProperties: KisProperties,
    private val kisTokenProvider: KisTokenProvider,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder
        .baseUrl(kisProperties.baseUrl)
        .build()

    fun isEnabled(): Boolean = kisProperties.enabled

    fun getCurrentPrice(symbol: String): KisCurrentPrice {
        if (!kisProperties.isConfigured()) {
            throw ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "KIS API 키가 설정되지 않았습니다.")
        }

        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/uapi/domestic-stock/v1/quotations/inquire-price")
                    .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                    .queryParam("FID_INPUT_ISCD", symbol)
                    .build()
            }
            .headers { headers ->
                headers.set(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                headers.setBearerAuth(kisTokenProvider.getAccessToken())
                headers.set("appkey", kisProperties.appKey)
                headers.set("appsecret", kisProperties.appSecret)
                headers.set("tr_id", "FHKST01010100")
            }
            .retrieve()
            .body(KisCurrentPriceResponse::class.java)
            ?: throw ApiException(HttpStatus.BAD_GATEWAY, "KIS 현재가 응답이 비어 있습니다.")

        if (response.resultCode != "0" || response.output == null) {
            throw ApiException(HttpStatus.BAD_GATEWAY, "KIS 현재가 조회에 실패했습니다: ${response.message}")
        }

        return KisCurrentPrice(
            price = response.output.currentPrice.toBigDecimalValue(),
            changeRate = response.output.changeRate.toBigDecimalValue(),
        )
    }

    private fun String.toBigDecimalValue(): BigDecimal {
        return replace(",", "").trim().ifBlank { "0" }.toBigDecimal()
    }
}

data class KisCurrentPrice(
    val price: BigDecimal,
    val changeRate: BigDecimal,
)

data class KisCurrentPriceResponse(
    @JsonProperty("rt_cd")
    val resultCode: String,
    @JsonProperty("msg1")
    val message: String = "",
    val output: KisCurrentPriceOutput?,
)

data class KisCurrentPriceOutput(
    @JsonProperty("stck_prpr")
    val currentPrice: String,
    @JsonProperty("prdy_ctrt")
    val changeRate: String,
)
