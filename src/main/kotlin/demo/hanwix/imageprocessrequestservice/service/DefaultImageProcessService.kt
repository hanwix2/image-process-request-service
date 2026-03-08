package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.util.HashEncoder
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultImageProcessService(
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val hashEncoder: HashEncoder
) : ImageProcessService {

    @Transactional
    override fun createTask(request: ImageProcessRequest): ImageProcessResponse {
        val hash = hashEncoder.sha256(request.imageUrl)

        if (imageProcessTaskRepository.findByImageUrlHash(hash) != null) {
            throw DuplicateImageRequestException(request.imageUrl)
        }

        val task = ImageProcessTask.create(imageUrl = request.imageUrl, imageUrlHash = hash)
        val savedTask = imageProcessTaskRepository.save(task)

        return ImageProcessResponse(
            taskId = savedTask.id,
            status = savedTask.status.name,
            createdAt = savedTask.createdAt
        )
    }
}
