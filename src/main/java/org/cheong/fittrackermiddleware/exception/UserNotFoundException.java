package org.cheong.fittrackermiddleware.exception;

/**
 * @author cheong
 * Custom exception for user not found
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
