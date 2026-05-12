package com.lpu.delivery_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
// GlobalExceptionHandler is a centralized exception handler for the Delivery Service application 
// that captures and handles various exceptions, providing consistent error responses to the client.
public class GlobalExceptionHandler {

    // Validation errors
// handleValidation captures MethodArgumentNotValidException, which occurs when validation on an argument annotated with @Valid fails.
// It extracts the first validation error message and returns it in a structured ErrorResponse with a 400 Bad Request status.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return new ResponseEntity<>(
                new ErrorResponse(message, HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {

        return new ResponseEntity<>(
                new ErrorResponse("Access Denied", HttpStatus.FORBIDDEN.value()),
                HttpStatus.FORBIDDEN
        );
    }

    // Runtime errors
//     handleRuntime captures any RuntimeException that occurs within the application and returns its message in an ErrorResponse with a 400 Bad Request status,
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {

        return new ResponseEntity<>(
                new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()),
                HttpStatus.BAD_REQUEST
        );
    }

    // General errors
//     handleGeneral captures any other exceptions that are not specifically handled by the previous methods and returns a generic error message in an ErrorResponse with a 500 Internal Server Error status.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        return new ResponseEntity<>(
                new ErrorResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR.value()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
