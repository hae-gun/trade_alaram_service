// 분산락 추상화입니다.
// 운영/로컬 다중 인스턴스 환경에서 스케줄러나 발송 작업의 중복 실행을 막는 용도로 사용합니다.
package com.tradealarm.global.lock

import java.time.Duration

interface DistributedLock {
    fun acquire(key: String, ttl: Duration): Boolean
    fun release(key: String)
}

