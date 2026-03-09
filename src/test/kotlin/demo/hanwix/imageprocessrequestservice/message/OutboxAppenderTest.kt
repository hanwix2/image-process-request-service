package demo.hanwix.imageprocessrequestservice.message

import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class OutboxAppenderTest @Autowired constructor(
    private val outboxAppender: OutboxAppender,
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository
) {

    @Test
    fun `appendImageProcessRequest 호출 시 OutboxMessage가 저장되고 payload에 requestId와 imageUrl이 포함된다`() {
        val aggregateId = 42L
        val imageUrl = "https://example.com/test.jpg"

        outboxAppender.appendImageProcessRequest(aggregateId = aggregateId, imageUrl = imageUrl)

        val messages = imageProcessOutboxMessageRepository.findAll()
        assertEquals(1, messages.size)
        assertFalse(messages[0].published)

        val payload = messages[0].payload
        assertTrue(payload.contains("\"requestId\":42"))
        assertTrue(payload.contains("\"imageUrl\":\"https://example.com/test.jpg\""))
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun `활성 트랜잭션 없이 호출 시 IllegalTransactionStateException이 발생한다`() {
        assertThrows<IllegalTransactionStateException> {
            outboxAppender.appendImageProcessRequest(aggregateId = 1L, imageUrl = "https://example.com/image.jpg")
        }
    }
}
