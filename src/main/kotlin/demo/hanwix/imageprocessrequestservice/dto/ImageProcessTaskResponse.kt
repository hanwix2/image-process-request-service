package demo.hanwix.imageprocessrequestservice.dto

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import java.time.LocalDateTime

data class ImageProcessTaskResponse(
    val id: Long,
    val imageUrl: String,
    val status: String,
    val workerJobId: String?,
    val resultUrl: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(task: ImageProcessTask) = ImageProcessTaskResponse(
            id = task.id,
            imageUrl = task.imageUrl,
            status = task.status.name,
            workerJobId = task.workerJobId,
            resultUrl = task.resultUrl,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
    }
}
