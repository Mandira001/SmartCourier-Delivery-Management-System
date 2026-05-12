package com.lpu.delivery_service.exception;

// ErrorResponse is a simple data transfer object (DTO) that encapsulates error information, 
// including a message and an HTTP status code, which is used to provide structured error responses 
// to clients when exceptions occur in the application.
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
