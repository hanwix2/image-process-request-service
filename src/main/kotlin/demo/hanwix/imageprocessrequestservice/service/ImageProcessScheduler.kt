package demo.hanwix.imageprocessrequestservice.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ImageProcessScheduler(
    private val imageProcessService: ImageProcessService,
    private val outboxAppender: OutboxAppender
) {

    @Scheduled(fixedDelay = 1000)
    fun publishPendingOutboxMessages() {
        outboxAppender.publishPendingMessages()
    }

    @Scheduled(fixedDelay = 30000)
    fun syncProcessingTaskStatuses() {
        imageProcessService.syncProcessingTaskStatuses()
    }
}
