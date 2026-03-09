package demo.hanwix.imageprocessrequestservice.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import demo.hanwix.imageprocessrequestservice.config.TestcontainersConfiguration
import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class ImageProcessKafkaConsumerTest @Autowired constructor(
    private val imageProcessKafkaConsumer: ImageProcessKafkaConsumer,
    private val imageProcessWorkerService: ImageProcessWorkerService,
    private val imageProcessTaskRepository: ImageProcessTaskRepository
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
    }

    @Test
    fun `consumeImageProcessRequest 호출 시 Mock Worker API를 호출하고 DB 상태가 PROCESSING으로 업데이트된다`() {
        val task = imageProcessTaskRepository.save(
            ImageProcessTask.create("https://example.com/image.jpg", "testhash")
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/auth/issue-key"))
                .willReturn(okJson("""{"apiKey":"test-api-key"}"""))
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/process"))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .willReturn(okJson("""{"jobId":"job-123","status":"PROCESSING"}"""))
        )

        imageProcessKafkaConsumer.consumeImageProcessRequest(
            """{"requestId":${task.id},"imageUrl":"https://example.com/image.jpg"}"""
        )

        val updated = imageProcessTaskRepository.findById(task.id).get()
        assertEquals(TaskStatus.PROCESSING, updated.status)
        assertEquals("job-123", updated.workerJobId)
    }

    @Test
    fun `processRequest 호출 시 Mock Worker API를 호출하고 DB 상태가 PROCESSING으로 업데이트된다`() {
        val task = imageProcessTaskRepository.save(
            ImageProcessTask.create("https://example.com/image.jpg", "testhash2")
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/auth/issue-key"))
                .willReturn(okJson("""{"apiKey":"test-api-key"}"""))
        )
        wireMockServer.stubFor(
            post(urlEqualTo("/process"))
                .withHeader("X-API-KEY", equalTo("test-api-key"))
                .willReturn(okJson("""{"jobId":"job-456","status":"PROCESSING"}"""))
        )

        imageProcessWorkerService.processRequest(task.id, "https://example.com/image.jpg")

        val updated = imageProcessTaskRepository.findById(task.id).get()
        assertEquals(TaskStatus.PROCESSING, updated.status)
        assertEquals("job-456", updated.workerJobId)
    }
}
