package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.config.KafkaConfig
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessWorkerService
import io.github.oshai.kotlinlogging.KotlinLogging
import tools.jackson.databind.ObjectMapper
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ImageProcessKafkaConsumer(
    private val imageProcessWorkerService: ImageProcessWorkerService,
    private val objectMapper: ObjectMapper
) {

    private val logger = KotlinLogging.logger {}

    @KafkaListener(topics = [KafkaConfig.TOPIC_IMAGE_PROCESS_REQUEST])
    fun consumeImageProcessRequest(message: String) {
        val payload = objectMapper.readValue(message, Map::class.java)
        val requestId = (payload["requestId"] as Number).toLong()
        val imageUrl = payload["imageUrl"] as String

        try {
            imageProcessWorkerService.processRequest(requestId, imageUrl)
        } catch (e: Exception) {
            logger.error { "Error while requesting image process - requestId: $requestId \nerror: ${e.message}" }
        }
    }
}
