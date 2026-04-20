package org.cheong.fittrackermiddleware.exception;

/**
 * @author cheong
 * Custom exception for API related errors
 */
public class ApiException extends RuntimeException{
    public ApiException(String message) {
        super(message);
    }
}
