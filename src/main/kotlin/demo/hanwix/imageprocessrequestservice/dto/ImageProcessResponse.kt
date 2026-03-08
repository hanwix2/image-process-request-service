package demo.hanwix.imageprocessrequestservice.dto

import java.time.LocalDateTime

data class ImageProcessResponse(
    val taskId: Long,
    val status: String,
    val createdAt: LocalDateTime
)
