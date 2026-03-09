package demo.hanwix.imageprocessrequestservice.controller

import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessTaskResponse
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.service.ImageProcessService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@WebMvcTest(ImageProcessController::class)
class ImageProcessControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var imageProcessService: ImageProcessService

    @Test
    fun `정상 요청 시 201과 PENDING 응답을 반환한다`() {
        given(imageProcessService.createTask(any())).willReturn(
            ImageProcessResponse(taskId = 1L, status = "PENDING", createdAt = LocalDateTime.now())
        )

        mockMvc.perform(
            post("/api/images/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"imageUrl": "https://example.com/image.jpg"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.taskId").value(1))
    }

    @Test
    fun `중복 요청 시 409를 반환한다`() {
        given(imageProcessService.createTask(any()))
            .willThrow(DuplicateImageRequestException("https://example.com/duplicate.jpg"))

        mockMvc.perform(
            post("/api/images/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"imageUrl": "https://example.com/duplicate.jpg"}""")
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `GET tasks 요청 시 200과 Page 응답을 반환한다`() {
        val now = LocalDateTime.now()
        val taskResponse = ImageProcessTaskResponse(
            id = 1L,
            imageUrl = "https://example.com/image.jpg",
            status = "PENDING",
            workerJobId = null,
            resultUrl = null,
            createdAt = now,
            updatedAt = now
        )
        given(imageProcessService.getTaskList(any())).willReturn(PageImpl(listOf(taskResponse)))

        mockMvc.perform(get("/api/images/tasks"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `GET tasks 요청 시 page, size 파라미터가 적용된다`() {
        given(imageProcessService.getTaskList(any())).willReturn(PageImpl(emptyList()))

        mockMvc.perform(get("/api/images/tasks").param("page", "2").param("size", "5"))
            .andExpect(status().isOk)
    }

    @Test
    fun `GET tasks 단건 조회 시 200과 task를 반환한다`() {
        val now = LocalDateTime.now()
        val taskResponse = ImageProcessTaskResponse(
            id = 42L,
            imageUrl = "https://example.com/image.jpg",
            status = "PROCESSING",
            workerJobId = "job-xyz",
            resultUrl = null,
            createdAt = now,
            updatedAt = now
        )
        given(imageProcessService.getTask(eq(42L))).willReturn(taskResponse)

        mockMvc.perform(get("/api/images/tasks/42"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.status").value("PROCESSING"))
            .andExpect(jsonPath("$.workerJobId").value("job-xyz"))
    }

    @Test
    fun `GET tasks 단건 조회 시 존재하지 않는 id면 404를 반환한다`() {
        given(imageProcessService.getTask(eq(999L)))
            .willThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mockMvc.perform(get("/api/images/tasks/999"))
            .andExpect(status().isNotFound)
    }
}
