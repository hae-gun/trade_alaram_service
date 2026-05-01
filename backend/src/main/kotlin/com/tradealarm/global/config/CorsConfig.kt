// 프론트엔드와 백엔드가 다른 포트에서 실행될 때 필요한 CORS 설정입니다.
// 허용 origin은 application.yml의 app.cors.allowed-origins 값으로 관리합니다.
package com.tradealarm.global.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = listOf("http://localhost:3000"),
)

@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties,
) {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = corsProperties.allowedOrigins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
