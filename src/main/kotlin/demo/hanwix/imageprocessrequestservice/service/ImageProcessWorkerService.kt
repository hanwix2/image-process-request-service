package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.client.MockWorkerClient
import demo.hanwix.imageprocessrequestservice.exception.TaskNotFoundException
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
            .orElseThrow { TaskNotFoundException(requestId) }

        task.startProcessing(response.jobId)
    }
}
