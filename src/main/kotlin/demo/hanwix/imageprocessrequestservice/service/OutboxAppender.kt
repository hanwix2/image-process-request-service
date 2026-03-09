package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.KafkaConfig
import demo.hanwix.imageprocessrequestservice.domain.ImageProcessOutboxMessage
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

private val logger = KotlinLogging.logger {}

@Component
class OutboxAppender(
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository,
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    @Transactional(propagation = Propagation.MANDATORY)
    fun appendImageProcessRequest(aggregateId: Long, imageUrl: String) {
        val payload = objectMapper.writeValueAsString(
            mapOf("requestId" to aggregateId, "imageUrl" to imageUrl)
        )
        imageProcessOutboxMessageRepository.save(ImageProcessOutboxMessage.create(aggregateId, payload))
    }

    @Transactional
    fun publishPendingMessages() {
        val messages = imageProcessOutboxMessageRepository.findAllByPublishedFalse()
        messages.forEach { message ->
            kafkaTemplate.send(KafkaConfig.TOPIC_IMAGE_PROCESS_REQUEST, message.payload).get()
            message.markAsPublished()
            logger.info { "Outbox message published - messageId: ${message.id}" }
        }
    }
}
