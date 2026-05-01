// Spring ApplicationContext가 정상적으로 뜨는지 확인하는 기본 통합 테스트입니다.
// 설정, JPA 매핑, Bean wiring 문제를 초기에 잡는 용도입니다.
package com.tradealarm

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TradeAlarmApplicationTests {
    @Test
    fun contextLoads() {
    }
}
