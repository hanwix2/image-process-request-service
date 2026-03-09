package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.dto.MockWorkerApiKeyResponse
import demo.hanwix.imageprocessrequestservice.dto.MockWorkerJobStatusResponse
import demo.hanwix.imageprocessrequestservice.dto.MockWorkerProcessResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class MockWorkerClient(
    @Value("\${mockworker.candidate-name}") private val candidateName: String,
    @Value("\${mockworker.email}") private val email: String,
    private val mockWorkerRestClient: RestClient
) {

    fun issueApiKey(): String =
        mockWorkerRestClient.post()
            .uri("/auth/issue-key")
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("candidateName" to candidateName, "email" to email))
            .retrieve()
            .body(MockWorkerApiKeyResponse::class.java)!!
            .apiKey

    fun processImage(apiKey: String, imageUrl: String): MockWorkerProcessResponse =
        mockWorkerRestClient.post()
            .uri("/process")
            .header("X-API-KEY", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("imageUrl" to imageUrl))
            .retrieve()
            .body(MockWorkerProcessResponse::class.java)!!

    fun getJobStatus(jobId: String): MockWorkerJobStatusResponse =
        mockWorkerRestClient.get()
            .uri("/process/{jobId}", jobId)
            .retrieve()
            .body(MockWorkerJobStatusResponse::class.java)!!
}
