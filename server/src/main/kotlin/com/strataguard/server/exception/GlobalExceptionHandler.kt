package com.strataguard.server.exception

import com.strataguard.server.controller.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

class NotFoundException(message: String) : RuntimeException(message)

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) = ErrorResponse(
        status = 404,
        error = "Not Found",
        message = ex.message ?: "Resource not found",
        path = request.requestURI,
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(ex: MethodArgumentNotValidException, request: HttpServletRequest): ErrorResponse {
        val firstError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = firstError?.let { "${it.field}: ${it.defaultMessage}" } ?: "Validation failed"
        return ErrorResponse(
            status = 400,
            error = "Bad Request",
            message = message,
            path = request.requestURI,
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException, request: HttpServletRequest) =
        ErrorResponse(
            status = ex.statusCode.value(),
            error = ex.reason ?: ex.statusCode.toString(),
            message = ex.reason ?: ex.message ?: "",
            path = request.requestURI,
        )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception, request: HttpServletRequest): ErrorResponse {
        log.error("Unhandled exception at ${request.requestURI}", ex)
        return ErrorResponse(
            status = 500,
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.requestURI,
        )
    }
}
