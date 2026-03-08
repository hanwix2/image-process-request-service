package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.KafkaConfig
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxScheduler(
    private val outboxMessageRepository: ImageProcessOutboxMessageRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publishImagePendingMessages() {
        val messages = outboxMessageRepository.findAllByPublishedFalse()
        messages.forEach { message ->
            kafkaTemplate.send(KafkaConfig.TOPIC_IMAGE_PROCESS_REQUEST, message.payload).get()
            message.markAsPublished()
        }
    }
}
