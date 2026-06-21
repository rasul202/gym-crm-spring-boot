package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.mapper.TrainingMapper;
import com.epam.gymcrmspringboot.model.*;
import com.epam.gymcrmspringboot.repository.TrainingCriteriaRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.TraineeService;
import com.epam.gymcrmspringboot.service.TrainerService;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import com.epam.gymcrmspringboot.service.impl.TrainingServiceImpl;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TrainingServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TrainingCriteriaRepository trainingCriteriaRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private AddTrainingRequest addTrainingRequest;
    private TrainingEntity trainingEntity;
    private TraineeEntity traineeEntity;
    private TrainerEntity trainerEntity;
    private TrainingTypeEntity trainingTypeEntity;
    private UserEntity trainerUserEntity;
    private UserEntity traineeUserEntity;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);

        addTrainingRequest = new AddTrainingRequest(
                "trainee.user",
                "trainer.user",
                "  Yoga Session  ",
                LocalDate.of(2024, 1, 15),
                60
        );

        trainerUserEntity = UserEntity.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .username("trainer.user")
                .password("trainerPassword")
                .isActive(true)
                .build();

        traineeUserEntity = UserEntity.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .username("trainee.user")
                .password("traineePassword")
                .isActive(true)
                .build();

        trainingTypeEntity = TrainingTypeEntity.builder()
                .id(1L)
                .trainingTypeName("Yoga")
                .build();

        traineeEntity = TraineeEntity.builder()
                .id(1L)
                .user(traineeUserEntity)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .address("123 Main St")
                .trainings(new ArrayList<>())
                .build();

        trainerEntity = TrainerEntity.builder()
                .id(1L)
                .user(trainerUserEntity)
                .specialization(trainingTypeEntity)
                .trainings(new ArrayList<>())
                .build();

        trainingEntity = TrainingEntity.builder()
                .id(1L)
                .trainee(traineeEntity)
                .trainer(trainerEntity)
                .trainingName("Yoga Session")
                .trainingType(trainingTypeEntity)
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();
    }

    @Nested
    @DisplayName("addTraining Tests")
    class AddTrainingTests {

        @Test
        @DisplayName("Should add training successfully")
        void testAddTrainingSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(traineeService.getTraineeByUsername("trainee.user")).thenReturn(traineeEntity);
            when(trainerService.getTrainerByUsername("trainer.user")).thenReturn(trainerEntity);
            when(trainingTypeService.getTrainingTypeByName("Yoga")).thenReturn(trainingTypeEntity);
            when(trainingRepository.save(any(TrainingEntity.class))).thenReturn(trainingEntity);

            // Act
            trainingService.addTraining(addTrainingRequest, authentication);

            // Assert
            ArgumentCaptor<TrainingEntity> captor = ArgumentCaptor.forClass(TrainingEntity.class);
            verify(trainingRepository).save(captor.capture());
            TrainingEntity saved = captor.getValue();
            assertEquals("Yoga Session", saved.getTrainingName());
            assertEquals(trainingTypeEntity, saved.getTrainingType());
            assertEquals(traineeEntity, saved.getTrainee());
            assertEquals(trainerEntity, saved.getTrainer());
            verify(requestValidator).validate(addTrainingRequest);
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid trainer credentials")
        void testAddTrainingThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainingService.addTraining(addTrainingRequest, authentication));
        }

        @Test
        @DisplayName("Should throw exception when trainer has no specialization")
        void testAddTrainingThrowsExceptionWhenTrainerHasNoSpecialization() {
            // Arrange
            trainerEntity.setSpecialization(null);

            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(traineeService.getTraineeByUsername("trainee.user")).thenReturn(traineeEntity);
            when(trainerService.getTrainerByUsername("trainer.user")).thenReturn(trainerEntity);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> trainingService.addTraining(addTrainingRequest, authentication));
            verify(trainingRepository, never()).save(any(TrainingEntity.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when trainee does not exist")
        void testAddTrainingThrowsExceptionWhenTraineeNotFound() {
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(traineeService.getTraineeByUsername("trainee.user"))
                    .thenThrow(new EntityNotFoundException("Trainee not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> trainingService.addTraining(addTrainingRequest, authentication));
            verifyNoInteractions(trainerService, trainingTypeService);
            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when trainer does not exist")
        void testAddTrainingThrowsExceptionWhenTrainerNotFound() {
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(traineeService.getTraineeByUsername("trainee.user")).thenReturn(traineeEntity);
            when(trainerService.getTrainerByUsername("trainer.user"))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));

            assertThrows(EntityNotFoundException.class,
                    () -> trainingService.addTraining(addTrainingRequest, authentication));
            verifyNoInteractions(trainingTypeService);
            verify(trainingRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTraineeTrainings Tests")
    class GetTraineeTrainingsTests {

        @Test
        @DisplayName("Should get trainee trainings successfully")
        void testGetTraineeTrainingsSuccess() {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteriaRequest = new GetTraineeTrainingsCriteriaRequest();
            criteriaRequest.setFromDate(LocalDate.of(2024, 1, 1));
            criteriaRequest.setToDate(LocalDate.of(2024, 12, 31));
            criteriaRequest.setTrainingType("Yoga");
            criteriaRequest.setTrainerName("  John  ");

            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainingCriteriaRepository.findTraineeTrainings(
                    eq("trainee.user"), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31)), eq("Yoga"), eq("John")))
                    .thenReturn(List.of(trainingEntity));
            when(trainingMapper.toGetTraineeTrainingsResponse(trainingEntity))
                    .thenReturn(new GetTraineeTrainingsResponse());

            // Act
            List<GetTraineeTrainingsResponse> result = trainingService.getTraineeTrainings(
                    "trainee.user", authentication, criteriaRequest);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(requestValidator).validate(criteriaRequest);
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testGetTraineeTrainingsThrowsExceptionForInvalidCredentials() {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteriaRequest = new GetTraineeTrainingsCriteriaRequest();

            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainingService.getTraineeTrainings("trainee.user", authentication, criteriaRequest));
        }

        @Test
        @DisplayName("Should handle null date filters")
        void testGetTraineeTrainingsWithNullDateFilters() {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteriaRequest = new GetTraineeTrainingsCriteriaRequest();

            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainingCriteriaRepository.findTraineeTrainings(
                    eq("trainee.user"), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(List.of(trainingEntity));
            when(trainingMapper.toGetTraineeTrainingsResponse(trainingEntity))
                    .thenReturn(new GetTraineeTrainingsResponse());

            // Act
            List<GetTraineeTrainingsResponse> result = trainingService.getTraineeTrainings(
                    "trainee.user", authentication, criteriaRequest);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTrainerTrainings Tests")
    class GetTrainerTrainingsTests {

        @Test
        @DisplayName("Should get trainer trainings successfully")
        void testGetTrainerTrainingsSuccess() {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteriaRequest = new GetTrainerTrainingsCriteriaRequest();
            criteriaRequest.setFromDate(LocalDate.of(2024, 1, 1));
            criteriaRequest.setToDate(LocalDate.of(2024, 12, 31));
            criteriaRequest.setTraineeName("  Jane  ");

            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainingCriteriaRepository.findTrainerTrainings(
                    eq("trainer.user"), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31)), eq("Jane")))
                    .thenReturn(List.of(trainingEntity));
            when(trainingMapper.toGetTrainerTrainingsResponse(trainingEntity))
                    .thenReturn(new GetTrainerTrainingsResponse());

            // Act
            List<GetTrainerTrainingsResponse> result = trainingService.getTrainerTrainings(
                    "trainer.user", authentication, criteriaRequest);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(requestValidator).validate(criteriaRequest);
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testGetTrainerTrainingsThrowsExceptionForInvalidCredentials() {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteriaRequest = new GetTrainerTrainingsCriteriaRequest();

            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainingService.getTrainerTrainings("trainer.user", authentication, criteriaRequest));
        }

        @Test
        @DisplayName("Should handle null date filters")
        void testGetTrainerTrainingsWithNullDateFilters() {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteriaRequest = new GetTrainerTrainingsCriteriaRequest();

            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainingCriteriaRepository.findTrainerTrainings(
                    eq("trainer.user"), isNull(), isNull(), isNull()))
                    .thenReturn(List.of(trainingEntity));
            when(trainingMapper.toGetTrainerTrainingsResponse(trainingEntity))
                    .thenReturn(new GetTrainerTrainingsResponse());

            // Act
            List<GetTrainerTrainingsResponse> result = trainingService.getTrainerTrainings(
                    "trainer.user", authentication, criteriaRequest);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("internal delegation Tests")
    class InternalDelegationTests {

        @Test
        @DisplayName("Should delegate deleteAllByTrainee to repository")
        void testDeleteAllByTraineeDelegatesToRepository() {
            // Act
            trainingService.deleteAllByTrainee(traineeEntity);

            // Assert
            verify(trainingRepository).deleteByTrainee(traineeEntity);
        }

        @Test
        @DisplayName("Should delegate saveAll to repository")
        void testSaveAllDelegatesToRepository() {
            // Arrange
            List<TrainingEntity> assignments = List.of(trainingEntity);

            // Act
            trainingService.saveAll(assignments);

            // Assert
            verify(trainingRepository).saveAll(assignments);
        }
    }

}
