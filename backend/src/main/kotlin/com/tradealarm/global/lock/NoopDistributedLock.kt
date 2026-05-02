// Redis가 없는 기본/test 프로필에서 사용하는 no-op lock 구현입니다.
// 단일 인스턴스 로컬 실행과 테스트에서는 항상 lock 획득에 성공하도록 둡니다.
package com.tradealarm.global.lock

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!local")
class NoopDistributedLock : DistributedLock {
    override fun acquire(key: String, ttl: Duration): Boolean = true

    override fun release(key: String) {
    }
}

