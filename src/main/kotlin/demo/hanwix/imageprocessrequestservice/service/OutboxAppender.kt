package demo.hanwix.imageprocessrequestservice.service

import tools.jackson.databind.ObjectMapper
import demo.hanwix.imageprocessrequestservice.domain.ImageProcessOutboxMessage
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxAppender(
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository,
    private val objectMapper: ObjectMapper
) {
    @Transactional(propagation = Propagation.MANDATORY)
    fun appendImageProcessRequest(aggregateId: Long, imageUrl: String) {
        val payload = objectMapper.writeValueAsString(
            mapOf("requestId" to aggregateId, "imageUrl" to imageUrl)
        )
        imageProcessOutboxMessageRepository.save(ImageProcessOutboxMessage.create(aggregateId, payload))
    }
}
