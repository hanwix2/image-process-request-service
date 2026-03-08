package demo.hanwix.imageprocessrequestservice.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "image_process_outbox_message")
class ImageProcessOutboxMessage private constructor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val aggregateId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Column(nullable = false)
    var published: Boolean = false,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(aggregateId: Long, payload: String): ImageProcessOutboxMessage =
            ImageProcessOutboxMessage(aggregateId = aggregateId, payload = payload)
    }

    fun markAsPublished() {
        this.published = true
    }
}
