package demo.hanwix.imageprocessrequestservice.controller

import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/images")
class ImageProcessController(
    private val imageProcessService: ImageProcessService
) {

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTask(@RequestBody request: ImageProcessRequest): ImageProcessResponse {
        return imageProcessService.createTask(request)
    }
}
