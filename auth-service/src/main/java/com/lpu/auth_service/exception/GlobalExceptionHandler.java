package com.lpu.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
// This class is a global exception handler for the authentication service. 
// It uses the @RestControllerAdvice annotation to handle exceptions thrown by any controller in the application
@RestControllerAdvice
public class GlobalExceptionHandler {

    // This method handles RuntimeExceptions, which are commonly thrown for various error conditions in the application. 
    // It creates an ErrorResponse object containing the exception message and a BAD_REQUEST status code,
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // This method is a catch-all handler for any other exceptions that are not specifically handled by other methods. 
    // It creates an ErrorResponse with a generic message and an INTERNAL_SERVER_ERROR status code,
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {

        ErrorResponse error = new ErrorResponse(
                "Something went wrong",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
