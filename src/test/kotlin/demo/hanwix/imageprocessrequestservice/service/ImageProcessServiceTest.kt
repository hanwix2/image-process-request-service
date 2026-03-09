package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class ImageProcessServiceTest @Autowired constructor(
    private val imageProcessService: ImageProcessService,
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository
) {

    @Test
    fun `정상 요청 시 PENDING 상태로 저장되고 응답을 반환한다`() {
        val request = ImageProcessRequest(imageUrl = "https://example.com/image.jpg")

        val response = imageProcessService.createTask(request)

        assertEquals(TaskStatus.PENDING.name, response.status)
        assertNotNull(response.taskId)
        assertNotNull(response.createdAt)
        assertEquals(1, imageProcessTaskRepository.count())
    }

    @Test
    fun `정상 요청 시 OutboxMessage가 생성된다`() {
        imageProcessService.createTask(ImageProcessRequest("https://example.com/outbox-test.jpg"))
        assertEquals(1, imageProcessOutboxMessageRepository.count())
        assertEquals(false, imageProcessOutboxMessageRepository.findAll()[0].published)
    }

    @Test
    fun `동일한 이미지 URL로 중복 요청 시 예외가 발생한다`() {
        val request = ImageProcessRequest(imageUrl = "https://example.com/duplicate.jpg")
        imageProcessService.createTask(request)

        assertThrows<DuplicateImageRequestException> {
            imageProcessService.createTask(request)
        }
    }

    @Test
    fun `빈 DB에서 getTaskList 호출 시 빈 Page를 반환한다`() {
        val pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending())
        val result = imageProcessService.getTaskList(pageable)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `여러 task 저장 후 getTaskList 호출 시 createdAt 내림차순으로 반환한다`() {
        imageProcessTaskRepository.save(ImageProcessTask.create("https://example.com/image1.jpg", "hash1"))
        Thread.sleep(10)
        imageProcessTaskRepository.save(ImageProcessTask.create("https://example.com/image2.jpg", "hash2"))
        Thread.sleep(10)
        imageProcessTaskRepository.save(ImageProcessTask.create("https://example.com/image3.jpg", "hash3"))

        val pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending())
        val result = imageProcessService.getTaskList(pageable)

        assertEquals(3, result.totalElements)
        assertEquals("https://example.com/image3.jpg", result.content[0].imageUrl)
        assertEquals("https://example.com/image1.jpg", result.content[2].imageUrl)
    }

    @Test
    fun `getTask 호출 시 해당 task를 반환한다`() {
        val task = imageProcessTaskRepository.save(
            ImageProcessTask.create("https://example.com/image.jpg", "hash-query")
        )

        val result = imageProcessService.getTask(task.id)

        assertEquals(task.id, result.id)
        assertEquals("https://example.com/image.jpg", result.imageUrl)
        assertEquals("PENDING", result.status)
    }

    @Test
    fun `존재하지 않는 id로 getTask 호출 시 ResponseStatusException 404가 발생한다`() {
        assertFailsWith<ResponseStatusException> {
            imageProcessService.getTask(999999L)
        }.also { ex ->
            assertEquals(404, ex.statusCode.value())
        }
    }
}
