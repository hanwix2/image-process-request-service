package demo.hanwix.imageprocessrequestservice.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateImageRequestException(imageUrl: String) :
    RuntimeException("Image process task already exists for: $imageUrl")
