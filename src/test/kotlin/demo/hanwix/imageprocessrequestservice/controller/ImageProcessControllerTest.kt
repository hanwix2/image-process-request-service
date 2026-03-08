package demo.hanwix.imageprocessrequestservice.controller

import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
            post("/api/images/process")
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
            post("/api/images/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"imageUrl": "https://example.com/duplicate.jpg"}""")
        )
            .andExpect(status().isConflict)
    }
}
