package demo.hanwix.imageprocessrequestservice.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessOutboxMessageRepository
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.time.LocalDateTime
import kotlin.test.assertEquals

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class ImageProcessSchedulerTest @Autowired constructor(
    private val imageProcessScheduler: ImageProcessScheduler,
    private val imageProcessService: ImageProcessService,
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val imageProcessOutboxMessageRepository: ImageProcessOutboxMessageRepository,
    private val jdbcTemplate: JdbcTemplate
) {
    companion object {
        private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun wireMockProperties(registry: DynamicPropertyRegistry) {
            registry.add("mockworker.base-url") { wireMockServer.baseUrl() }
        }
    }

    @AfterEach
    fun cleanup() {
        wireMockServer.resetAll()
        imageProcessOutboxMessageRepository.deleteAll()
        imageProcessTaskRepository.deleteAll()
    }

    @Test
    fun `publishPendingOutboxMessages 실행 후 OutboxMessage가 published=true로 업데이트된다`() {
        imageProcessService.createTask(ImageProcessRequest("https://example.com/image.jpg"))
        assertEquals(false, imageProcessOutboxMessageRepository.findAll()[0].published)

        imageProcessScheduler.publishPendingOutboxMessages()

        assertEquals(true, imageProcessOutboxMessageRepository.findAll()[0].published)
    }

    @Test
    fun `syncProcessingTaskStatuses 실행 시 PROCESSING task가 COMPLETED 응답을 받으면 COMPLETED로 업데이트된다`() {
        val task = saveProcessingTask("https://example.com/image1.jpg", "hash-sched-1", "job-abc")

        wireMockServer.stubFor(
            get(urlEqualTo("/process/job-abc"))
                .willReturn(okJson("""{"jobId":"job-abc","status":"COMPLETED","result":"https://result.com/out.jpg"}"""))
        )

        imageProcessScheduler.syncProcessingTaskStatuses()

        val updated = imageProcessTaskRepository.findById(task.id).get()
        assertEquals(TaskStatus.COMPLETED, updated.status)
        assertNotNull(updated.resultMessage)
    }

    @Test
    fun `syncProcessingTaskStatuses 실행 시 PROCESSING task가 FAILED 응답을 받으면 FAILED로 업데이트된다`() {
        val task = saveProcessingTask("https://example.com/image2.jpg", "hash-sched-2", "job-def")

        wireMockServer.stubFor(
            get(urlEqualTo("/process/job-def"))
                .willReturn(okJson("""{"jobId":"job-def","status":"FAILED","result":null}"""))
        )

        imageProcessScheduler.syncProcessingTaskStatuses()

        val updated = imageProcessTaskRepository.findById(task.id).get()
        assertEquals(TaskStatus.FAILED, updated.status)
    }

    @Test
    fun `syncProcessingTaskStatuses 실행 시 생성 시간이 1시간을 초과한 PROCESSING task는 업데이트 대상에서 제외된다`() {
        val task = saveProcessingTask("https://example.com/image3.jpg", "hash-sched-3", "job-old")

        jdbcTemplate.update(
            "UPDATE image_process_task SET created_at = ? WHERE id = ?",
            LocalDateTime.now().minusHours(2),
            task.id
        )

        imageProcessScheduler.syncProcessingTaskStatuses()

        val notUpdated = imageProcessTaskRepository.findById(task.id).get()
        assertEquals(TaskStatus.PROCESSING, notUpdated.status)
    }

    private fun saveProcessingTask(imageUrl: String, hash: String, jobId: String): ImageProcessTask {
        val task = imageProcessTaskRepository.save(ImageProcessTask.create(imageUrl, hash))
        task.startProcessing(jobId)
        return imageProcessTaskRepository.save(task)
    }
}
