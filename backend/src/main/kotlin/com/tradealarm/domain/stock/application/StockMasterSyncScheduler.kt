// 종목 마스터를 시작 시점과 정해진 주기에 동기화합니다.
package com.tradealarm.domain.stock.application

import com.tradealarm.domain.stock.infra.StockMasterProperties
import com.tradealarm.global.lock.DistributedLock
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class StockMasterSyncScheduler(
    private val stockMasterProperties: StockMasterProperties,
    private val stockMasterSyncService: StockMasterSyncService,
    private val distributedLock: DistributedLock,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    fun syncOnStartup() {
        if (stockMasterProperties.syncOnStartup) {
            syncSafely()
        }
    }

    @Scheduled(cron = "\${app.stock-master.cron:0 30 7 * * MON-FRI}")
    fun syncScheduled() {
        syncSafely()
    }

    private fun syncSafely() {
        if (!stockMasterProperties.enabled) {
            return
        }

        val lockKey = "tradealarm:lock:stock-master-sync"
        if (!distributedLock.acquire(lockKey, Duration.ofMinutes(10))) {
            return
        }

        try {
            stockMasterSyncService.sync()
        } catch (error: Exception) {
            log.warn("종목 마스터 동기화 실패: reason={}", error.message)
        } finally {
            distributedLock.release(lockKey)
        }
    }
}
