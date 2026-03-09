package demo.hanwix.imageprocessrequestservice.service

import demo.hanwix.imageprocessrequestservice.domain.ImageProcessTask
import demo.hanwix.imageprocessrequestservice.domain.TaskStatus
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessRequest
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessResponse
import demo.hanwix.imageprocessrequestservice.dto.ImageProcessTaskResponse
import demo.hanwix.imageprocessrequestservice.dto.MockWorkerJobStatusResponse
import demo.hanwix.imageprocessrequestservice.exception.DuplicateImageRequestException
import demo.hanwix.imageprocessrequestservice.repository.ImageProcessTaskRepository
import demo.hanwix.imageprocessrequestservice.util.HashEncoder
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class ImageProcessService(
    private val imageProcessTaskRepository: ImageProcessTaskRepository,
    private val outboxAppender: OutboxAppender,
    private val hashEncoder: HashEncoder,
    private val mockWorkerClient: MockWorkerClient
) {

    @Transactional
    fun createTask(request: ImageProcessRequest): ImageProcessResponse {
        val hash = hashEncoder.sha256(request.imageUrl)

        if (imageProcessTaskRepository.findByImageUrlHash(hash) != null) {
            throw DuplicateImageRequestException(request.imageUrl)
        }

        val task = ImageProcessTask.create(imageUrl = request.imageUrl, imageUrlHash = hash)
        val savedTask = imageProcessTaskRepository.save(task)

        outboxAppender.appendImageProcessRequest(savedTask.id, savedTask.imageUrl)

        return ImageProcessResponse(
            taskId = savedTask.id,
            status = savedTask.status.name,
            createdAt = savedTask.createdAt
        )
    }

    fun getTaskList(pageable: Pageable): Page<ImageProcessTaskResponse> =
        imageProcessTaskRepository.findAllByOrderByCreatedAtDesc(pageable).map { ImageProcessTaskResponse.from(it) }

    fun getTask(id: Long): ImageProcessTaskResponse {
        val task = imageProcessTaskRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: $id") }
        return ImageProcessTaskResponse.from(task)
    }

    @Transactional
    fun syncProcessingTaskStatuses() {
        val threshold = LocalDateTime.now().minusHours(1)
        val tasks = imageProcessTaskRepository.findByStatusAndCreatedAtAfter(TaskStatus.PROCESSING, threshold)
            .filter { it.workerJobId != null }

        if (tasks.isEmpty()) return

        val results: List<Pair<ImageProcessTask, MockWorkerJobStatusResponse>> = runBlocking {
            tasks.map { task ->
                async {
                    try {
                        task to mockWorkerClient.getJobStatus(task.workerJobId!!)
                    } catch (e: Exception) {
                        logger.warn(e) { "Failed to get job status for task ${task.id}, jobId=${task.workerJobId}" }
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }

        results.forEach { (task, response) ->
            when (response.status) {
                "COMPLETED" -> task.complete(response.result ?: "")
                "FAILED" -> task.fail()
            }
        }
    }
}
