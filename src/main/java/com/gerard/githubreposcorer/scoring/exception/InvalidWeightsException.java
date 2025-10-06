package com.gerard.githubreposcorer.scoring.exception;

/**
 * Exception thrown when the provided weights for scoring are invalid.
 */
public class InvalidWeightsException extends RuntimeException {
    
    public InvalidWeightsException(String message) {
        super(message);
    }
    
    public InvalidWeightsException(String message, Throwable cause) {
        super(message, cause);
    }
}
