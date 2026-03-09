package demo.hanwix.imageprocessrequestservice.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ImageProcessScheduler(
    private val imageProcessService: ImageProcessService,
    private val outboxAppender: OutboxAppender
) {

    // Transactional Outbox 패턴 적용 & 메시지 발행을 위한 스케줄러 (1분 주기)
    @Scheduled(fixedDelay = 1000)
    fun publishPendingOutboxMessages() {
        outboxAppender.publishPendingMessages()
    }

    // 이미지 처리 진행중인 작업의 상태를 업데이트하는 스케줄러 (5분 주기)
    @Scheduled(fixedDelay = 5000)
    fun syncProcessingTaskStatuses() {
        imageProcessService.syncProcessingTaskStatuses()
    }
}
