package com.epam.gymcrmspringboot.validation;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.service.TraineeService;
import com.epam.gymcrmspringboot.service.TrainerService;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class TrainerTraineeRegistrationValidator {

    static Logger LOGGER = LoggerFactory.getLogger(TrainerTraineeRegistrationValidator.class);

    TrainerService trainerService;
    TraineeService traineeService;

    public TrainerTraineeRegistrationValidator(@Lazy TrainerService trainerService,
                                               @Lazy TraineeService traineeService) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
    }

    // Validates trainer registration by ensuring the same person does not already exist as a trainee.
    public void validateTrainerRegistration(CreateTrainerRequest request) {
        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();

        if (traineeService.isRegisteredAsTrainee(firstName, lastName)) {
            String message = "Cannot register as trainer: user is already registered as trainee [firstName="
                    + firstName + ", lastName=" + lastName + "]";
            LOGGER.warn(message);
            throw new UserAlreadyRegisteredInOppositeRoleException(message);
        }

        LOGGER.debug("Trainer registration cross-role validation passed for firstName={} lastName={}", firstName, lastName);
    }

    // Validates trainee registration by ensuring the same person does not already exist as a trainer.
    public void validateTraineeRegistration(CreateTraineeRequest request) {
        String firstName = request.getFirstName().trim();
        String lastName = request.getLastName().trim();

        if (trainerService.isRegisteredAsTrainer(firstName, lastName)) {
            String message = "Cannot register as trainee: user is already registered as trainer [firstName="
                    + firstName + ", lastName=" + lastName + "]";
            LOGGER.warn(message);
            throw new UserAlreadyRegisteredInOppositeRoleException(message);
        }

        LOGGER.debug("Trainee registration cross-role validation passed for firstName={} lastName={}", firstName, lastName);
    }
}