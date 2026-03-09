package demo.hanwix.imageprocessrequestservice.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateImageRequestException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicate(e: DuplicateImageRequestException): ErrorResponse =
        ErrorResponse(HttpStatus.CONFLICT.value(), e.message ?: "Duplicate request")

    @ExceptionHandler(TaskNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(e: TaskNotFoundException): ErrorResponse =
        ErrorResponse(HttpStatus.NOT_FOUND.value(), e.message ?: "Not found")
}
