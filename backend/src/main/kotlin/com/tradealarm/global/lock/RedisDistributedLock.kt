// Redis 기반 분산락 구현입니다.
// SET NX + TTL 방식으로 lock을 획득하고 작업 종료 시 key를 삭제합니다.
package com.tradealarm.global.lock

import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("local")
class RedisDistributedLock(
    private val redisTemplate: StringRedisTemplate,
) : DistributedLock {
    override fun acquire(key: String, ttl: Duration): Boolean {
        return redisTemplate.opsForValue().setIfAbsent(key, "locked", ttl) == true
    }

    override fun release(key: String) {
        redisTemplate.delete(key)
    }
}

