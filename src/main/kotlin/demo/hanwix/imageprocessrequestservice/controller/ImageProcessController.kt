package demo.hanwix.imageprocessrequestservice.controller

import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessTaskResponse
import demo.hanwix.imageprocessrequestservice.service.ImageProcessService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/images")
class ImageProcessController(
    private val imageProcessService: ImageProcessService
) {

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@RequestBody request: ImageProcessRequest): ImageProcessResponse {
        return imageProcessService.createTask(request)
    }

    @GetMapping("/tasks")
    fun getTaskList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<ImageProcessTaskResponse> {
        val pageable = PageRequest.of(page, size)
        return imageProcessService.getTaskList(pageable)
    }

    @GetMapping("/tasks/{taskId}")
    fun getTask(@PathVariable taskId: Long): ImageProcessTaskResponse {
        return imageProcessService.getTask(taskId)
    }
}
