package org.cheong.fittrackermiddleware.exception;

public class ApiException extends RuntimeException{
    public ApiException(String message) {
        super(message);
    }
}
