package demo.hanwix.imageprocessrequestservice.repository

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ImageProcessTaskRepository : JpaRepository<ImageProcessTask, Long> {
    fun findByImageUrlHash(hash: String): ImageProcessTask?
    fun findByStatusAndCreatedAtAfter(status: TaskStatus, threshold: LocalDateTime): List<ImageProcessTask>
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<ImageProcessTask>
}
