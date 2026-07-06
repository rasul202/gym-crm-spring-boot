package com.epam.gymcrmspringboot.exception;

public class TrainerWorkloadException extends RuntimeException {
    public TrainerWorkloadException(String message) {
        super(message);
    }

    public TrainerWorkloadException(String message, Throwable cause) {
        super(message, cause);
    }
}