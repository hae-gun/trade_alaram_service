// 한국투자증권 Open API 접속 설정입니다.
// 민감한 app key/secret은 파일에 직접 저장하지 않고 환경변수로 주입합니다.
package com.tradealarm.domain.market.infra

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.kis")
data class KisProperties(
    val enabled: Boolean = false,
    val baseUrl: String = "https://openapivts.koreainvestment.com:29443",
    val appKey: String = "",
    val appSecret: String = "",
) {
    fun isConfigured(): Boolean = appKey.isNotBlank() && appSecret.isNotBlank()
}

