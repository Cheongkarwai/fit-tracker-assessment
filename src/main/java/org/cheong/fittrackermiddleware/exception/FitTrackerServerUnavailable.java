package org.cheong.fittrackermiddleware.exception;

/**
 * @author cheong
 * Custom exception for Fit Tracker server unavailable
 */
public class FitTrackerServerUnavailable extends RuntimeException {
    public FitTrackerServerUnavailable(String message) {
        super(message);
    }
}
