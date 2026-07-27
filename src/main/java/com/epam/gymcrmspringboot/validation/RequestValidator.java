package com.epam.gymcrmspringboot.validation;

import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    //validate Trainer Workload request before sending to the queue
    public List<String> validate(TrainerWorkloadRequest request) {
        List<String> invalidFields = new ArrayList<>();

        if (request == null) {
            invalidFields.add("request");
            return invalidFields;
        }

        if (isBlank(request.getTrainerUsername())) {
            invalidFields.add("trainerUsername");
        }
        if (isBlank(request.getTrainerFirstName())) {
            invalidFields.add("trainerFirstName");
        }
        if (isBlank(request.getTrainerLastName())) {
            invalidFields.add("trainerLastName");
        }
        if (request.getIsActive() == null) {
            invalidFields.add("isActive");
        }
        if (request.getTrainingDate() == null) {
            invalidFields.add("trainingDate");
        }
        if (request.getTrainingDuration() <= 0) {
            invalidFields.add("trainingDuration");
        }
        if (request.getActionType() == null) {
            invalidFields.add("actionType");
        }

        return invalidFields;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
