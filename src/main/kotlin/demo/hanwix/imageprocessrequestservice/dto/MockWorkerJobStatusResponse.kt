package demo.hanwix.imageprocessrequestservice.dto

data class MockWorkerJobStatusResponse(
    val jobId: String,
    val status: String,
    val result: String?
)
