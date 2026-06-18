package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.mapper.TraineeMapper;
import com.epam.gymcrmspringboot.mapper.UserMapper;
import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.TraineeRepository;
import com.epam.gymcrmspringboot.service.TrainerService;
import com.epam.gymcrmspringboot.service.TrainingService;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.service.impl.TraineeServiceImpl;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import com.epam.gymcrmspringboot.validation.TrainerTraineeRegistrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("TraineeServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainerTraineeRegistrationValidator trainerTraineeRegistrationValidator;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    private CreateTraineeRequest createTraineeRequest;
    private UpdateTraineeProfileRequest updateTraineeProfileRequest;
    private TraineeEntity traineeEntity;
    private UserEntity userEntity;
    private GetTraineeProfileResponse getTraineeProfileResponse;
    private UpdateTraineeProfileResponse updateTraineeProfileResponse;
    private RegistrationResponse registrationResponse;

    @BeforeEach
    void setUp() {
        createTraineeRequest = new CreateTraineeRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St"
        );

        userEntity = UserEntity.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("password123")
                .isActive(true)
                .build();

        traineeEntity = TraineeEntity.builder()
                .id(1L)
                .user(userEntity)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .trainings(new ArrayList<>())
                .build();

        getTraineeProfileResponse = new GetTraineeProfileResponse();
        getTraineeProfileResponse.setFirstName("John");
        getTraineeProfileResponse.setLastName("Doe");
        getTraineeProfileResponse.setDateOfBirth(LocalDate.of(1990, 1, 1));
        getTraineeProfileResponse.setAddress("123 Main St");
        getTraineeProfileResponse.setIsActive(true);
        getTraineeProfileResponse.setTrainers(new ArrayList<>());

        updateTraineeProfileResponse = new UpdateTraineeProfileResponse();
        updateTraineeProfileResponse.setUsername("John.Doe");
        updateTraineeProfileResponse.setFirstName("Jane");
        updateTraineeProfileResponse.setLastName("Doe");
        updateTraineeProfileResponse.setDateOfBirth(LocalDate.of(1990, 1, 1));
        updateTraineeProfileResponse.setAddress("456 Oak Ave");
        updateTraineeProfileResponse.setIsActive(true);
        updateTraineeProfileResponse.setTrainers(new ArrayList<>());

        registrationResponse = new RegistrationResponse("John.Doe", "password123");

        updateTraineeProfileRequest = new UpdateTraineeProfileRequest();
        updateTraineeProfileRequest.setFirstName("Jane");
        updateTraineeProfileRequest.setAddress("456 Oak Ave");
    }

    @Nested
    @DisplayName("registerTrainee Tests")
    class RegisterTraineeTests {


        @Test
        @DisplayName("Should throw exception when user creation fails")
        void testRegisterTraineeFailsWhenUserCreationFails() {
            // Arrange
            com.epam.gymcrmspringboot.dto.request.CreateUserRequest createUserRequest =
                    mock(com.epam.gymcrmspringboot.dto.request.CreateUserRequest.class);

            when(userMapper.toCreateUserRequest(createTraineeRequest)).thenReturn(createUserRequest);
            when(userService.createUserProfile(createUserRequest)).thenReturn(null);

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> traineeService.registerTrainee(createTraineeRequest));
        }

        @Test
        @DisplayName("Should validate request before registration")
        void testRegisterTraineeValidatesRequest() {
            // Arrange
            com.epam.gymcrmspringboot.dto.request.CreateUserRequest createUserRequest =
                    mock(com.epam.gymcrmspringboot.dto.request.CreateUserRequest.class);
            com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse createUserProfileResponse =
                    new com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse(1L, "John.Doe", "password123");

            when(userMapper.toCreateUserRequest(createTraineeRequest))
                    .thenReturn(createUserRequest);
            when(userService.createUserProfile(createUserRequest))
                    .thenReturn(createUserProfileResponse);
            when(traineeRepository.save(any(TraineeEntity.class)))
                    .thenReturn(traineeEntity);

            // Act
            traineeService.registerTrainee(createTraineeRequest);

            // Assert
            verify(requestValidator, times(1)).validate(createTraineeRequest);
        }

        @Test
        @DisplayName("Should fail when user is already registered as trainer")
        void testRegisterTraineeFailsWhenUserAlreadyRegisteredAsTrainer() {
            doThrow(new UserAlreadyRegisteredInOppositeRoleException("already trainer"))
                    .when(trainerTraineeRegistrationValidator)
                    .validateTraineeRegistration(createTraineeRequest);

            assertThrows(UserAlreadyRegisteredInOppositeRoleException.class,
                    () -> traineeService.registerTrainee(createTraineeRequest));

            verify(requestValidator).validate(createTraineeRequest);
            verify(trainerTraineeRegistrationValidator).validateTraineeRegistration(createTraineeRequest);
            verifyNoInteractions(userService, traineeRepository);
        }
    }

    @Nested
    @DisplayName("updateTrainee Tests")
    class UpdateTraineeTests {

        @Test
        @DisplayName("Should update trainee successfully")
        void testUpdateTraineeSuccess() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(true);
            when(traineeRepository.findByUserUsernameAndUserIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(traineeEntity));
            when(traineeRepository.save(any(TraineeEntity.class)))
                    .thenReturn(traineeEntity);
            when(traineeMapper.toUpdateTraineeProfileResponse(traineeEntity))
                    .thenReturn(updateTraineeProfileResponse);

            // Act
            UpdateTraineeProfileResponse result = traineeService.updateTrainee(
                    "John.Doe", "password123", updateTraineeProfileRequest);

            // Assert
            assertNotNull(result);
            verify(traineeRepository).save(any(TraineeEntity.class));
        }


        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testUpdateTraineeThrowsExceptionForInvalidCredentials() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> traineeService.updateTrainee("John.Doe", "password123", updateTraineeProfileRequest));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when no updatable fields are provided")
        void testUpdateTraineeThrowsExceptionWhenNoUpdatableFieldsProvided() {
            // Arrange
            UpdateTraineeProfileRequest emptyRequest = new UpdateTraineeProfileRequest();

            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(true);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.updateTrainee("John.Doe", "password123", emptyRequest));
            verify(traineeRepository, never()).findByUserUsernameAndUserIsActiveTrue(anyString());
            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deactivateTrainee Tests")
    class DeactivateTraineeTests {

        @Test
        @DisplayName("Should deactivate trainee successfully")
        void testDeactivateTraineeSuccess() {
            // Arrange
            when(userService.authenticateAnyUser(any(LoginRequest.class))).thenReturn(true);
            when(userService.deactivateUserProfile("John.Doe")).thenReturn(true);

            // Act
            traineeService.deactivateTrainee("John.Doe", "password123");

            // Assert
            verify(userService).deactivateUserProfile("John.Doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testDeactivateTraineeThrowsExceptionForInvalidCredentials() {
            // Arrange
            when(userService.authenticateAnyUser(any(LoginRequest.class))).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> traineeService.deactivateTrainee("John.Doe", "wrongPassword"));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trainee is already deactivated")
        void testDeactivateTraineeThrowsExceptionWhenAlreadyDeactivated() {
            // Arrange
            when(userService.authenticateAnyUser(any(LoginRequest.class))).thenReturn(true);
            when(userService.deactivateUserProfile("John.Doe")).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> traineeService.deactivateTrainee("John.Doe", "password123"));
            verify(userService).deactivateUserProfile("John.Doe");
        }
    }

    @Nested
    @DisplayName("activateTrainee Tests")
    class ActivateTraineeTests {

        @Test
        @DisplayName("Should activate trainee successfully")
        void testActivateTraineeSuccess() {
            // Arrange
            when(userService.authenticateAnyUser(any(LoginRequest.class))).thenReturn(true);
            when(userService.activateUserProfile("John.Doe")).thenReturn(true);

            // Act
            traineeService.activateTrainee("John.Doe", "password123");

            // Assert
            verify(userService).activateUserProfile("John.Doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testActivateTraineeThrowsExceptionForInvalidCredentials() {
            // Arrange
            when(userService.authenticateAnyUser(any(LoginRequest.class))).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> traineeService.activateTrainee("John.Doe", "wrongPassword"));
        }
    }

    @Nested
    @DisplayName("getTraineeByUsername Tests")
    class GetTraineeByUsernameTests {

        @Test
        @DisplayName("Should get trainee by username successfully")
        void testGetTraineeByUsernameSuccess() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(true);
            when(traineeRepository.findByUserUsernameAndUserIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(traineeEntity));
            when(traineeMapper.toGetTraineeProfileResponse(traineeEntity))
                    .thenReturn(getTraineeProfileResponse);

            // Act
            GetTraineeProfileResponse result = traineeService.getTraineeByUsername("John.Doe", "password123");

            // Assert
            assertNotNull(result);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
        }

        @Test
        @DisplayName("Should throw exception for null or blank username")
        void testGetTraineeByUsernameThrowsExceptionForNullUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.getTraineeByUsername(null, "password"));
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.getTraineeByUsername("   ", "password"));
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testGetTraineeByUsernameThrowsExceptionForInvalidCredentials() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> traineeService.getTraineeByUsername("John.Doe", "wrongPassword"));
        }
    }

    @Nested
    @DisplayName("deleteTrainee Tests")
    class DeleteTraineeTests {

        @Test
        @DisplayName("Should delete trainee successfully")
        void testDeleteTraineeSuccess() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(true);
            when(traineeRepository.findByUserUsernameAndUserIsActiveTrueWithTrainings("John.Doe"))
                    .thenReturn(Optional.of(traineeEntity));

            // Act
            traineeService.deleteTrainee("John.Doe", "password123");

            // Assert
            verify(traineeRepository).delete(traineeEntity);
        }

        @Test
        @DisplayName("Should throw exception for null or blank username")
        void testDeleteTraineeThrowsExceptionForNullUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.deleteTrainee(null, "password"));
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.deleteTrainee("   ", "password"));
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testDeleteTraineeThrowsExceptionForInvalidCredentials() {
            // Arrange
            when(userService.authenticateActiveUser(any(LoginRequest.class))).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> traineeService.deleteTrainee("John.Doe", "wrongPassword"));
        }
    }

    @Nested
    @DisplayName("getTraineeByUsername (internal) Tests")
    class GetTraineeByUsernameInternalTests {

        @Test
        @DisplayName("Should get trainee by username for internal use")
        void testGetTraineeByUsernameInternalSuccess() {
            // Arrange
            when(traineeRepository.findByUserUsernameAndUserIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(traineeEntity));

            // Act
            TraineeEntity result = traineeService.getTraineeByUsername("John.Doe");

            // Assert
            assertNotNull(result);
            assertEquals(traineeEntity, result);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when trainee not found")
        void testGetTraineeByUsernameInternalThrowsException() {
            // Arrange
            when(traineeRepository.findByUserUsernameAndUserIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> traineeService.getTraineeByUsername("nonexistent"));
        }
    }

    @Nested
    @DisplayName("updateTraineeTrainers Tests")
    class UpdateTraineeTrainersTests {

        @Test
        @DisplayName("Should update trainee trainers successfully")
        void testUpdateTraineeTrainersSuccess() {
            // Arrange
            List<String> trainerUsernames = List.of("trainer1.user", "trainer2.user");
            TrainerEntity trainer1 = TrainerEntity.builder()
                    .id(1L)
                    .user(UserEntity.builder().id(2L).username("trainer1.user").isActive(true).build())
                    .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                    .build();
            TrainerEntity trainer2 = TrainerEntity.builder()
                    .id(2L)
                    .user(UserEntity.builder().id(3L).username("trainer2.user").isActive(true).build())
                    .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                    .build();

            doReturn(true).when(userService).authenticateActiveUser(any(LoginRequest.class));
            doReturn(Optional.of(traineeEntity)).when(traineeRepository)
                    .findByUserUsernameAndUserIsActiveTrueWithTrainings("John.Doe");
            doReturn(List.of(trainer1, trainer2)).when(trainerService)
                    .getAllTrainersUsernamesIn(anyList());

            TrainerEntity oldTrainer = TrainerEntity.builder()
                    .id(3L)
                    .user(UserEntity.builder().id(4L).username("old.trainer").isActive(true).build())
                    .specialization(TrainingTypeEntity.builder().trainingTypeName("Yoga").build())
                    .build();

            TrainingEntity existingWithTrainer1 = TrainingEntity.builder()
                    .id(10L)
                    .trainee(traineeEntity)
                    .trainer(trainer1)
                    .trainingName("Real Training")
                    .trainingType(trainer1.getSpecialization())
                    .trainingDate(LocalDate.of(2024, 1, 1))
                    .trainingDuration(45)
                    .build();

            TrainingEntity obsoleteTraining = TrainingEntity.builder()
                    .id(11L)
                    .trainee(traineeEntity)
                    .trainer(oldTrainer)
                    .trainingName("Outdated Training")
                    .trainingType(oldTrainer.getSpecialization())
                    .trainingDate(LocalDate.of(2024, 1, 2))
                    .trainingDuration(45)
                    .build();

            traineeEntity.setTrainings(new ArrayList<>(List.of(existingWithTrainer1, obsoleteTraining)));
            doReturn(traineeEntity).when(traineeRepository).save(traineeEntity);

            // Act
            List<TrainerSummary> result = traineeService.updateTraineeTrainers(
                    "John.Doe", "password123", trainerUsernames);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(2, traineeEntity.getTrainings().size());
            assertTrue(traineeEntity.getTrainings().stream()
                    .anyMatch(training -> "trainer1.user".equals(training.getTrainer().getUser().getUsername())));
            assertTrue(traineeEntity.getTrainings().stream()
                    .anyMatch(training -> "trainer2.user".equals(training.getTrainer().getUser().getUsername())));
            assertFalse(traineeEntity.getTrainings().stream()
                    .anyMatch(training -> "old.trainer".equals(training.getTrainer().getUser().getUsername())));

            verify(traineeRepository).save(traineeEntity);
            verifyNoInteractions(trainingService);
        }

        @Test
        @DisplayName("Should throw exception when trainer not found")
        void testUpdateTraineeTrainersThrowsExceptionForNonexistentTrainer() {
            // Arrange
            List<String> trainerUsernames = List.of("nonexistent.trainer");

            doReturn(true).when(userService).authenticateActiveUser(any(LoginRequest.class));
            doReturn(new ArrayList<TrainerEntity>()).when(trainerService)
                    .getAllTrainersUsernamesIn(anyList());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> traineeService.updateTraineeTrainers("John.Doe", "password123", trainerUsernames));
            verify(traineeRepository, never()).findByUserUsernameAndUserIsActiveTrueWithTrainings(any());
            verify(traineeRepository, never()).save(any());
            verifyNoInteractions(trainingService);
        }

        @Test
        @DisplayName("Should throw exception when trainerUsernames list is empty")
        void testUpdateTraineeTrainersThrowsExceptionForEmptyList() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> traineeService.updateTraineeTrainers("John.Doe", "password123", List.of()));
            verifyNoInteractions(userService, traineeRepository, trainerService, trainingService);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trainer has no specialization")
        void testUpdateTraineeTrainersThrowsExceptionWhenTrainerHasNoSpecialization() {
            // Arrange
            List<String> trainerUsernames = List.of("trainer1.user");
            TrainerEntity trainerWithoutSpecialization = TrainerEntity.builder()
                    .id(1L)
                    .user(UserEntity.builder().id(2L).username("trainer1.user").isActive(true).build())
                    .specialization(null)
                    .build();

            doReturn(true).when(userService).authenticateActiveUser(any(LoginRequest.class));
            doReturn(List.of(trainerWithoutSpecialization)).when(trainerService)
                    .getAllTrainersUsernamesIn(anyList());
            traineeEntity.setTrainings(new ArrayList<>());
            doReturn(Optional.of(traineeEntity)).when(traineeRepository)
                    .findByUserUsernameAndUserIsActiveTrueWithTrainings("John.Doe");

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> traineeService.updateTraineeTrainers("John.Doe", "password123", trainerUsernames));
            verify(traineeRepository, never()).save(any());
            verifyNoInteractions(trainingService);
        }
    }

    @Nested
    @DisplayName("getAvailableTrainersForTrainee Tests")
    class GetAvailableTrainersForTraineeTests {

        @Test
        @DisplayName("Should get available trainers for trainee successfully")
        void testGetAvailableTrainersForTraineeSuccess() {
            // Arrange
            List<TrainerSummary> availableTrainers = List.of(
                    new TrainerSummary("trainer3.user", "Trainer", "Three", "Pilates")
            );

            when(trainerService.getAvailableTrainersForTrainee("John.Doe", "password123"))
                    .thenReturn(availableTrainers);

            // Act
            List<TrainerSummary> result = traineeService.getAvailableTrainersForTrainee("John.Doe", "password123");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(trainerService).getAvailableTrainersForTrainee("John.Doe", "password123");
        }
    }
}
