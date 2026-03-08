package demo.hanwix.imageprocessrequestservice.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.kafka.KafkaContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> = MySQLContainer("mysql:8")

    @Bean
    fun kafkaContainer(): KafkaContainer =
        KafkaContainer("apache/kafka-native:3.8.0")

    @Bean
    fun kafkaProperties(kafkaContainer: KafkaContainer): DynamicPropertyRegistrar =
        DynamicPropertyRegistrar { registry ->
            registry.add("spring.kafka.bootstrap-servers") { kafkaContainer.bootstrapServers }
        }

}
