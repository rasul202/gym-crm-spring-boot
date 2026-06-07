package com.epam.gymcrmspringboot.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestValidator {

    private Validator validator;

    @Autowired
    public void setValidator(Validator validator){
        this.validator = validator;
    }

    public <T> void validate(T request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }
}

