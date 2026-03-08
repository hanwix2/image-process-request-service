package demo.hanwix.imageprocessrequestservice.repository

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessOutboxMessage
import org.springframework.data.jpa.repository.JpaRepository

interface ImageProcessOutboxMessageRepository : JpaRepository<ImageProcessOutboxMessage, Long> {
    fun findAllByPublishedFalse(): List<ImageProcessOutboxMessage>
}
