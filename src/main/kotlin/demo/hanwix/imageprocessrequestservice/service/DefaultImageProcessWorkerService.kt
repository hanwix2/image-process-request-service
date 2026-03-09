package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import demo.hanwix.imageprocessrequestservice.service.ports.`in`.ImageProcessWorkerService
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class DefaultImageProcessWorkerService(
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val mockWorkerClient: MockWorkerClient,
) : ImageProcessWorkerService {

    @Transactional
    override fun processRequest(requestId: Long, imageUrl: String) {
        val apiKey = mockWorkerClient.issueApiKey()
        val response = mockWorkerClient.processImage(apiKey, imageUrl)

        val task = imageProcessTaskRepository.findById(requestId)
            .orElseThrow { NoSuchElementException("Task not found: $requestId") }

        task.startProcessing(response.jobId)
    }
}
