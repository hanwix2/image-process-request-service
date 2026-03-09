package demo.hanwix.imageprocessrequestservice.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ImageProcessTaskTest {

    private fun createTask() = ImageProcessTask.create(
        imageUrl = "https://example.com/image.jpg",
        imageUrlHash = "abc123hash"
    )

    @Test
    fun `초기 상태는 PENDING이다`() {
        val request = createTask()
        assertEquals(TaskStatus.PENDING, request.status)
        assertNull(request.workerJobId)
        assertNull(request.resultMessage)
    }

    @Test
    fun `startProcessing 호출 시 상태가 PROCESSING으로 변경된다`() {
        val request = createTask()
        request.startProcessing("job-001")
        assertEquals(TaskStatus.PROCESSING, request.status)
        assertEquals("job-001", request.workerJobId)
    }

    @Test
    fun `complete 호출 시 상태가 COMPLETED로 변경되고 resultMessage가 저장된다`() {
        val request = createTask()
        val resultMsg = "Image processed successfully"
        request.startProcessing("job-001")
        request.complete(resultMsg)
        assertEquals(TaskStatus.COMPLETED, request.status)
        assertEquals(resultMsg, request.resultMessage)
    }

    @Test
    fun `fail 호출 시 상태가 FAILED로 변경된다`() {
        val request = createTask()
        request.startProcessing("job-001")
        request.fail()
        assertEquals(TaskStatus.FAILED, request.status)
        assertNotNull(request.updatedAt)
    }
}
