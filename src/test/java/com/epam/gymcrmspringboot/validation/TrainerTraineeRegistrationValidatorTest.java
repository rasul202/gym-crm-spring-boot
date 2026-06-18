package com.epam.gymcrmspringboot.validation;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.service.TraineeService;
import com.epam.gymcrmspringboot.service.TrainerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerTraineeRegistrationValidator Tests")
class TrainerTraineeRegistrationValidatorTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TrainerTraineeRegistrationValidator validator;

    @Test
    @DisplayName("Should throw when registering trainer already present as trainee")
    void validateTrainerRegistrationThrowsWhenTraineeExists() {
        CreateTrainerRequest request = new CreateTrainerRequest("John", "Doe", "Yoga");
        when(traineeService.isRegisteredAsTrainee("John", "Doe")).thenReturn(true);

        assertThrows(UserAlreadyRegisteredInOppositeRoleException.class,
                () -> validator.validateTrainerRegistration(request));
    }

    @Test
    @DisplayName("Should pass when registering trainer not present as trainee")
    void validateTrainerRegistrationPassesWhenTraineeMissing() {
        CreateTrainerRequest request = new CreateTrainerRequest("John", "Doe", "Yoga");
        when(traineeService.isRegisteredAsTrainee("John", "Doe")).thenReturn(false);

        validator.validateTrainerRegistration(request);

        verify(traineeService).isRegisteredAsTrainee("John", "Doe");
    }

    @Test
    @DisplayName("Should throw when registering trainee already present as trainer")
    void validateTraineeRegistrationThrowsWhenTrainerExists() {
        CreateTraineeRequest request = new CreateTraineeRequest("Jane", "Smith", LocalDate.of(1990, 1, 1), "Address");
        when(trainerService.isRegisteredAsTrainer("Jane", "Smith")).thenReturn(true);

        assertThrows(UserAlreadyRegisteredInOppositeRoleException.class,
                () -> validator.validateTraineeRegistration(request));
    }

    @Test
    @DisplayName("Should pass when registering trainee not present as trainer")
    void validateTraineeRegistrationPassesWhenTrainerMissing() {
        CreateTraineeRequest request = new CreateTraineeRequest("Jane", "Smith", LocalDate.of(1990, 1, 1), "Address");
        when(trainerService.isRegisteredAsTrainer("Jane", "Smith")).thenReturn(false);

        validator.validateTraineeRegistration(request);

        verify(trainerService).isRegisteredAsTrainer("Jane", "Smith");
    }
}

