package demo.hanwix.imageprocessrequestservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class KafkaConfig {
    companion object {
        const val TOPIC_IMAGE_PROCESS_REQUEST = "image-process-request"
    }

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Bean
    fun kafkaAdmin(): KafkaAdmin =
        KafkaAdmin(mapOf("bootstrap.servers" to bootstrapServers))

    @Bean
    fun producerFactory(): ProducerFactory<String, String> =
        DefaultKafkaProducerFactory(
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
            )
        )

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> = KafkaTemplate(producerFactory())

    @Bean
    fun imageProcessRequestTopic(): NewTopic =
        TopicBuilder.name(TOPIC_IMAGE_PROCESS_REQUEST).build()
}
