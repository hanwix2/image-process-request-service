package demo.hanwix.imageprocessrequestservice.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "image_process_job",
    indexes = [Index(name = "uq_image_url_hash", columnList = "imageUrlHash", unique = true)]
)
class ImageProcessJob private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, columnDefinition = "TEXT")
    val imageUrl: String,

    @Column(nullable = false, length = 64, unique = true)
    val imageUrlHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: JobStatus = JobStatus.PENDING,

    @Column(nullable = true)
    var workerJobId: String? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    var resultUrl: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(imageUrl: String, imageUrlHash: String): ImageProcessJob =
            ImageProcessJob(imageUrl = imageUrl, imageUrlHash = imageUrlHash)
    }

    fun startProcessing(workerJobId: String) {
        this.status = JobStatus.PROCESSING
        this.workerJobId = workerJobId
        this.updatedAt = LocalDateTime.now()
    }

    fun complete(resultUrl: String) {
        this.status = JobStatus.COMPLETED
        this.resultUrl = resultUrl
        this.updatedAt = LocalDateTime.now()
    }

    fun fail() {
        this.status = JobStatus.FAILED
        this.updatedAt = LocalDateTime.now()
    }
}
