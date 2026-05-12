package com.lpu.auth_service.exception;
// This class represents the structure of an error response that will be sent back to the client when an exception occurs. 
// It contains a message describing the error and an HTTP status code indicating the type of error.
public class ErrorResponse {

    private String message;
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
