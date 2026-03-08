package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class OutboxSchedulerTest @Autowired constructor(
    private val imageProcessService: ImageProcessService,
    private val outboxScheduler: OutboxScheduler,
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository,
    private val imageProcessTaskRepository: ImageProcessTaskRepository
) {
    @AfterEach
    fun cleanup() {
        imageProcessOutboxMessageRepository.deleteAll()
        imageProcessTaskRepository.deleteAll()
    }

    @Test
    fun `Scheduler 실행 후 OutboxMessage가 published=true로 업데이트된다`() {
        imageProcessService.createTask(ImageProcessRequest("https://example.com/image.jpg"))
        assertEquals(false, imageProcessOutboxMessageRepository.findAll()[0].published)

        outboxScheduler.publishImagePendingMessages()

        assertEquals(true, imageProcessOutboxMessageRepository.findAll()[0].published)
    }
}
