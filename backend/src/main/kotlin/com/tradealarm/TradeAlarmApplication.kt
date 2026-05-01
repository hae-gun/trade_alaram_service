// 백엔드 애플리케이션의 진입점입니다.
// Spring Boot 자동 설정, ConfigurationProperties 스캔, 알림 평가 스케줄러를 활성화합니다.
package com.tradealarm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
class TradeAlarmApplication

fun main(args: Array<String>) {
    runApplication<TradeAlarmApplication>(*args)
}
