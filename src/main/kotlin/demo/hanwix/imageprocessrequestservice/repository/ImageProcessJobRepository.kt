package demo.hanwix.imageprocessrequestservice.repository

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessJob
import demo.hanwix.imageprocessrequestservice.domain.JobStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ImageProcessJobRepository : JpaRepository<ImageProcessJob, Long> {
    fun findByImageUrlHash(hash: String): ImageProcessJob?
    fun findByStatusAndCreatedAtAfter(status: JobStatus, threshold: LocalDateTime): List<ImageProcessJob>
}
