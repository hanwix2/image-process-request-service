package demo.hanwix.imageprocessrequestservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class MockWorkerClientConfig {

    @Bean
    fun mockWorkerRestClient(@Value("\${mockworker.base-url}") baseUrl: String): RestClient =
        RestClient.builder()
            .requestFactory(SimpleClientHttpRequestFactory())
            .baseUrl(baseUrl)
            .build()

}
