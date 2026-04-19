package org.cheong.fittrackermiddleware.exception;

public class FitTrackerServerDown extends RuntimeException {
    public FitTrackerServerDown(String message) {
        super(message);
    }
}
