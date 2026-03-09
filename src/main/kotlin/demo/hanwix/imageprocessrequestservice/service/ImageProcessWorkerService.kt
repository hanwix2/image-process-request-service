package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ImageProcessWorkerService(
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val mockWorkerClient: MockWorkerClient,
) {

    @Transactional
    fun processRequest(requestId: Long, imageUrl: String) {
        val apiKey = mockWorkerClient.issueApiKey()
        val response = mockWorkerClient.processImage(apiKey, imageUrl)

        val task = imageProcessTaskRepository.findById(requestId)
            .orElseThrow { NoSuchElementException("Task not found: $requestId") }

        task.startProcessing(response.jobId)
    }
}
