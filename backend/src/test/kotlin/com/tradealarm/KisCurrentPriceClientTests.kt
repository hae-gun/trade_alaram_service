// 한국투자증권 현재가 클라이언트의 HTTP 계약과 응답 매핑을 검증합니다.
// 실제 API 키나 외부망을 사용하지 않고 MockRestServiceServer로 요청/응답을 대체합니다.
package com.tradealarm

import com.tradealarm.domain.market.infra.KisCurrentPriceClient
import com.tradealarm.domain.market.infra.KisProperties
import com.tradealarm.domain.market.infra.KisTokenProvider
import com.tradealarm.global.exception.ApiException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class KisCurrentPriceClientTests {
    @Test
    fun `현재가 API 응답을 가격 스냅샷 값으로 변환한다`() {
        val restClientBuilder = RestClient.builder()
        val server = MockRestServiceServer.bindTo(restClientBuilder).build()
        val properties = KisProperties(
            enabled = true,
            baseUrl = "https://kis.example",
            appKey = "test-app-key",
            appSecret = "test-app-secret",
        )
        val tokenProvider = KisTokenProvider(properties, restClientBuilder)
        val client = KisCurrentPriceClient(properties, tokenProvider, restClientBuilder)

        server.expect(ExpectedCount.once(), requestTo("https://kis.example/oauth2/tokenP"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.grant_type").value("client_credentials"))
            .andExpect(jsonPath("$.appkey").value("test-app-key"))
            .andExpect(jsonPath("$.appsecret").value("test-app-secret"))
            .andRespond(
                withSuccess(
                    """{"access_token":"mock-access-token","expires_in":3600}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        server.expect(
            ExpectedCount.once(),
            requestTo("https://kis.example/uapi/domestic-stock/v1/quotations/inquire-price?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=005930"),
        )
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer mock-access-token"))
            .andExpect(header("appkey", "test-app-key"))
            .andExpect(header("appsecret", "test-app-secret"))
            .andExpect(header("tr_id", "FHKST01010100"))
            .andRespond(
                withSuccess(
                    """
                    {
                      "rt_cd": "0",
                      "msg1": "정상처리 되었습니다.",
                      "output": {
                        "stck_prpr": "71000",
                        "prdy_ctrt": "1.25"
                      }
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        val currentPrice = client.getCurrentPrice("005930")

        assertThat(currentPrice.price).isEqualByComparingTo("71000")
        assertThat(currentPrice.changeRate).isEqualByComparingTo("1.25")
        server.verify()
    }

    @Test
    fun `KIS 키가 없으면 현재가 조회를 중단한다`() {
        val properties = KisProperties(enabled = true, baseUrl = "https://kis.example")
        val restClientBuilder = RestClient.builder()
        val tokenProvider = KisTokenProvider(properties, restClientBuilder)
        val client = KisCurrentPriceClient(properties, tokenProvider, restClientBuilder)

        assertThatThrownBy { client.getCurrentPrice("005930") }
            .isInstanceOf(ApiException::class.java)
            .hasMessageContaining("KIS")
    }
}
