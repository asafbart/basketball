package com.nba.stats.exception;

public class InvalidStatsException extends RuntimeException {
    public InvalidStatsException(String message) {
        super(message);
    }
}