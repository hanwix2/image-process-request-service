package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class DefaultImageProcessServiceTest @Autowired constructor(
    private val imageProcessService: ImageProcessService,
    private val imageProcessTaskRepository: ImageProcessTaskRepository
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
    fun `동일한 이미지 URL로 중복 요청 시 예외가 발생한다`() {
        val request = ImageProcessRequest(imageUrl = "https://example.com/duplicate.jpg")
        imageProcessService.createTask(request)

        assertThrows<DuplicateImageRequestException> {
            imageProcessService.createTask(request)
        }
    }
}
