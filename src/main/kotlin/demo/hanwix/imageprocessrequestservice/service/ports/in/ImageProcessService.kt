package demo.hanwix.imageprocessrequestservice.service.ports.`in`

import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse

interface ImageProcessService {
    fun createTask(request: ImageProcessRequest): ImageProcessResponse
}
