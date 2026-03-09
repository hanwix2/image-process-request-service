package demo.hanwix.imageprocessrequestservice.service.ports.`in`

interface ImageProcessWorkerService {
    fun processRequest(requestId: Long, imageUrl: String)
}
