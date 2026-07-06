package com.epam.gymcrmspringboot.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExternalServiceException extends TrainerWorkloadException {

    int statusCode;
    String sourceService;

    public ExternalServiceException(int statusCode, String sourceService, String message) {
        super(message);
        this.statusCode = statusCode;
        this.sourceService = sourceService;
    }
}
