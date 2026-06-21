package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.mapper.TrainerMapper;
import com.epam.gymcrmspringboot.mapper.UserMapper;
import com.epam.gymcrmspringboot.model.TrainerEntity;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.TrainerRepository;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import com.epam.gymcrmspringboot.service.UserService;
import com.epam.gymcrmspringboot.service.impl.TrainerServiceImpl;
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
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TrainerServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TrainingTypeService trainingTypeService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TrainerTraineeRegistrationValidator trainerTraineeRegistrationValidator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    private CreateTrainerRequest createTrainerRequest;
    private UpdateTrainerProfileRequest updateTrainerRequest;
    private TrainerEntity trainerEntity;
    private UserEntity userEntity;
    private TrainingTypeEntity trainingTypeEntity;
    private GetTrainerProfileResponse getTrainerProfileResponse;
    private UpdateTrainerProfileResponse updateTrainerProfileResponse;
    private TrainerSummary trainerSummary;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);

        createTrainerRequest = new CreateTrainerRequest(
                "John",
                "Smith",
                "Yoga"
        );

        updateTrainerRequest = new UpdateTrainerProfileRequest();
        updateTrainerRequest.setFirstName("Jane");
        updateTrainerRequest.setLastName("Smith");
        updateTrainerRequest.setSpecialization("Pilates");
        updateTrainerRequest.setIsActive(true);

        userEntity = UserEntity.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .username("John.Smith")
                .password("password123")
                .isActive(true)
                .build();

        trainingTypeEntity = TrainingTypeEntity.builder()
                .id(1L)
                .trainingTypeName("Yoga")
                .build();

        trainerEntity = TrainerEntity.builder()
                .id(1L)
                .user(userEntity)
                .specialization(trainingTypeEntity)
                .trainings(new ArrayList<>())
                .build();

        getTrainerProfileResponse = new GetTrainerProfileResponse();
        getTrainerProfileResponse.setFirstName("John");
        getTrainerProfileResponse.setLastName("Smith");
        getTrainerProfileResponse.setSpecialization("Yoga");
        getTrainerProfileResponse.setIsActive(true);
        getTrainerProfileResponse.setTrainees(new ArrayList<>());

        updateTrainerProfileResponse = new UpdateTrainerProfileResponse();
        updateTrainerProfileResponse.setUsername("John.Smith");
        updateTrainerProfileResponse.setFirstName("Jane");
        updateTrainerProfileResponse.setLastName("Smith");
        updateTrainerProfileResponse.setSpecialization("Yoga");
        updateTrainerProfileResponse.setIsActive(true);

        trainerSummary = new TrainerSummary("John.Smith", "John", "Smith", "Yoga");
    }

    @Nested
    @DisplayName("registerTrainer Tests")
    class RegisterTrainerTests {

        @Test
        @DisplayName("Should register trainer successfully")
        void testRegisterTrainerSuccess() {
            // Arrange
            com.epam.gymcrmspringboot.dto.request.CreateUserRequest createUserRequest =
                    mock(com.epam.gymcrmspringboot.dto.request.CreateUserRequest.class);
            com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse createUserProfileResponse =
                    new com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse(1L, "John.Smith", "password123");

            when(trainingTypeService.getTrainingTypeByName("Yoga"))
                    .thenReturn(trainingTypeEntity);
            when(userMapper.toCreateUserRequest(createTrainerRequest))
                    .thenReturn(createUserRequest);
            when(userService.createUserProfile(any()))
                    .thenReturn(createUserProfileResponse);
            when(trainerRepository.save(any(TrainerEntity.class)))
                    .thenReturn(trainerEntity);

            // Act
            RegistrationResponse result = trainerService.registerTrainer(createTrainerRequest);

            // Assert
            assertNotNull(result);
            assertEquals("John.Smith", result.getUsername());
            assertEquals("password123", result.getPassword());
            verify(trainerRepository).save(any(TrainerEntity.class));
            verify(requestValidator).validate(createTrainerRequest);
        }

        @Test
        @DisplayName("Should throw exception when training type not found")
        void testRegisterTrainerThrowsExceptionForNonexistentTrainingType() {
            // Arrange
            when(trainingTypeService.getTrainingTypeByName("Yoga"))
                    .thenThrow(new EntityNotFoundException("Training type not found"));

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> trainerService.registerTrainer(createTrainerRequest));
        }

        @Test
        @DisplayName("Should throw exception when user creation fails")
        void testRegisterTrainerFailsWhenUserCreationFails() {
            // Arrange
            com.epam.gymcrmspringboot.dto.request.CreateUserRequest createUserRequest =
                    mock(com.epam.gymcrmspringboot.dto.request.CreateUserRequest.class);
            when(trainingTypeService.getTrainingTypeByName("Yoga"))
                    .thenReturn(trainingTypeEntity);
            when(userMapper.toCreateUserRequest(createTrainerRequest)).thenReturn(createUserRequest);
            when(userService.createUserProfile(any())).thenReturn(null);

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> trainerService.registerTrainer(createTrainerRequest));
        }

        @Test
        @DisplayName("Should fail when user is already registered as trainee")
        void testRegisterTrainerFailsWhenUserAlreadyRegisteredAsTrainee() {
            doThrow(new UserAlreadyRegisteredInOppositeRoleException("already trainee"))
                    .when(trainerTraineeRegistrationValidator)
                    .validateTrainerRegistration(createTrainerRequest);

            assertThrows(UserAlreadyRegisteredInOppositeRoleException.class,
                    () -> trainerService.registerTrainer(createTrainerRequest));

            verify(requestValidator).validate(createTrainerRequest);
            verify(trainerTraineeRegistrationValidator).validateTrainerRegistration(createTrainerRequest);
            verifyNoInteractions(trainingTypeService, userService, trainerRepository);
        }
    }

    @Nested
    @DisplayName("updateTrainer Tests")
    class UpdateTrainerTests {

        @Test
        @DisplayName("Should update trainer successfully")
        void testUpdateTrainerSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findByUserUsername("John.Smith"))
                    .thenReturn(Optional.of(trainerEntity));
            when(trainerRepository.save(any(TrainerEntity.class)))
                    .thenReturn(trainerEntity);
            when(trainerMapper.toUpdateTrainerProfileResponse(trainerEntity))
                    .thenReturn(updateTrainerProfileResponse);

            // Act
            UpdateTrainerProfileResponse result = trainerService.updateTrainer("John.Smith", authentication, updateTrainerRequest);

            // Assert
            assertNotNull(result);
            verify(trainerRepository).save(any(TrainerEntity.class));
        }

        @Test
        @DisplayName("Should deactivate profile when isActive is false")
        void testUpdateTrainerDeactivatesProfileWhenIsActiveFalse() {
            // Arrange
            updateTrainerRequest.setIsActive(false);
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findByUserUsername("John.Smith"))
                    .thenReturn(Optional.of(trainerEntity));
            when(trainerRepository.save(any(TrainerEntity.class)))
                    .thenReturn(trainerEntity);
            when(trainerMapper.toUpdateTrainerProfileResponse(trainerEntity))
                    .thenReturn(updateTrainerProfileResponse);

            // Act
            trainerService.updateTrainer("John.Smith", authentication, updateTrainerRequest);

            // Assert
            verify(userService).deactivateUserProfile("John.Smith");
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testUpdateTrainerThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainerService.updateTrainer("John.Smith", authentication, updateTrainerRequest));
        }

        @Test
        @DisplayName("Should throw exception when trainer not found")
        void testUpdateTrainerThrowsExceptionWhenTrainerNotFound() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findByUserUsername("John.Smith"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> trainerService.updateTrainer("John.Smith", authentication, updateTrainerRequest));
        }

        @Test
        @DisplayName("Should activate profile when isActive is true")
        void testUpdateTrainerActivatesProfileWhenIsActiveTrue() {
            // Arrange
            updateTrainerRequest.setFirstName(null);
            updateTrainerRequest.setLastName(null);
            updateTrainerRequest.setIsActive(true);
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findByUserUsername("John.Smith")).thenReturn(Optional.of(trainerEntity));
            when(trainerRepository.save(any(TrainerEntity.class))).thenReturn(trainerEntity);
            when(trainerMapper.toUpdateTrainerProfileResponse(trainerEntity)).thenReturn(updateTrainerProfileResponse);

            // Act
            trainerService.updateTrainer("John.Smith", authentication, updateTrainerRequest);

            // Assert
            verify(userService).activateUserProfile("John.Smith");
        }
    }

    @Nested
    @DisplayName("getTrainerByUsername Tests")
    class GetTrainerByUsernameTests {

        @Test
        @DisplayName("Should get trainer by username successfully")
        void testGetTrainerByUsernameSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findByUserUsername("John.Smith"))
                    .thenReturn(Optional.of(trainerEntity));
            when(trainerMapper.toGetTrainerProfileResponse(trainerEntity))
                    .thenReturn(getTrainerProfileResponse);

            // Act
            GetTrainerProfileResponse result = trainerService.getTrainerByUsername("John.Smith", authentication);

            // Assert
            assertNotNull(result);
            assertEquals("John", result.getFirstName());
            assertEquals("Smith", result.getLastName());
        }

        @Test
        @DisplayName("Should throw exception for null or blank username")
        void testGetTrainerByUsernameThrowsExceptionForNullUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> trainerService.getTrainerByUsername(null, authentication));
            assertThrows(IllegalArgumentException.class,
                    () -> trainerService.getTrainerByUsername("   ", authentication));
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testGetTrainerByUsernameThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainerService.getTrainerByUsername("John.Smith", authentication));
        }
    }

    @Nested
    @DisplayName("deactivateTrainer Tests")
    class DeactivateTrainerTests {

        @Test
        @DisplayName("Should deactivate trainer successfully")
        void testDeactivateTrainerSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(userService.deactivateUserProfile("John.Smith")).thenReturn(true);

            // Act
            trainerService.deactivateTrainer("John.Smith", authentication);

            // Assert
            verify(userService).deactivateUserProfile("John.Smith");
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testDeactivateTrainerThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainerService.deactivateTrainer("John.Smith", authentication));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trainer is already deactivated")
        void testDeactivateTrainerThrowsExceptionWhenAlreadyDeactivated() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(userService.deactivateUserProfile("John.Smith")).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> trainerService.deactivateTrainer("John.Smith", authentication));
            verify(userService).deactivateUserProfile("John.Smith");
        }
    }

    @Nested
    @DisplayName("activateTrainer Tests")
    class ActivateTrainerTests {

        @Test
        @DisplayName("Should activate trainer successfully")
        void testActivateTrainerSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(userService.activateUserProfile("John.Smith")).thenReturn(true);

            // Act
            trainerService.activateTrainer("John.Smith", authentication);

            // Assert
            verify(userService).activateUserProfile("John.Smith");
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testActivateTrainerThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainerService.activateTrainer("John.Smith", authentication));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when trainer is already active")
        void testActivateTrainerThrowsExceptionWhenAlreadyActive() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(userService.activateUserProfile("John.Smith")).thenReturn(false);

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> trainerService.activateTrainer("John.Smith", authentication));
            verify(userService).activateUserProfile("John.Smith");
        }
    }

    @Nested
    @DisplayName("getAvailableTrainersForTrainee Tests")
    class GetAvailableTrainersForTraineeTests {

        @Test
        @DisplayName("Should get available trainers successfully")
        void testGetAvailableTrainersForTraineeSuccess() {
            // Arrange
            doNothing().when(authenticationService).assertAuthenticatedUser(any(), any());
            when(trainerRepository.findAvailableTrainersForTrainee("trainee.username"))
                    .thenReturn(List.of(trainerEntity));
            when(trainerMapper.trainerEntityToTrainerSummary(trainerEntity)).thenReturn(trainerSummary);

            // Act
            List<TrainerSummary> result = trainerService.getAvailableTrainersForTrainee("trainee.username", authentication);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for null or blank trainee username")
        void testGetAvailableTrainersThrowsExceptionForNullUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> trainerService.getAvailableTrainersForTrainee(null, authentication));
            assertThrows(IllegalArgumentException.class,
                    () -> trainerService.getAvailableTrainersForTrainee("   ", authentication));
        }

        @Test
        @DisplayName("Should throw AuthenticationException for invalid credentials")
        void testGetAvailableTrainersThrowsExceptionForInvalidCredentials() {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(authenticationService).assertAuthenticatedUser(any(), any());

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> trainerService.getAvailableTrainersForTrainee("trainee.username", authentication));
        }
    }

    @Nested
    @DisplayName("getAllTrainersUsernamesIn Tests")
    class GetAllTrainersUsernamesInTests {

        @Test
        @DisplayName("Should get trainers by usernames successfully")
        void testGetAllTrainersUsernamesInSuccess() {
            // Arrange
            List<String> usernames = List.of("trainer1", "trainer2");
            when(trainerRepository.findByUserUsernameInAndUserIsActiveTrue(usernames))
                    .thenReturn(List.of(trainerEntity));

            // Act
            List<TrainerEntity> result = trainerService.getAllTrainersUsernamesIn(usernames);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when no trainers found")
        void testGetAllTrainersUsernamesInReturnsEmptyList() {
            // Arrange
            List<String> usernames = List.of("nonexistent");
            when(trainerRepository.findByUserUsernameInAndUserIsActiveTrue(usernames))
                    .thenReturn(new ArrayList<>());

            // Act
            List<TrainerEntity> result = trainerService.getAllTrainersUsernamesIn(usernames);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTrainerByUsername (internal) Tests")
    class GetTrainerByUsernameInternalTests {

        @Test
        @DisplayName("Should get trainer by username for internal use")
        void testGetTrainerByUsernameInternalSuccess() {
            // Arrange
            when(trainerRepository.findByUserUsername("John.Smith"))
                    .thenReturn(Optional.of(trainerEntity));

            // Act
            TrainerEntity result = trainerService.getTrainerByUsername("John.Smith");

            // Assert
            assertNotNull(result);
            assertEquals(trainerEntity, result);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when trainer not found")
        void testGetTrainerByUsernameInternalThrowsException() {
            // Arrange
            when(trainerRepository.findByUserUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> trainerService.getTrainerByUsername("nonexistent"));
        }
    }

    @Nested
    @DisplayName("isRegisteredAsTrainer Tests")
    class IsRegisteredAsTrainerTests {

        @Test
        @DisplayName("Should trim names and return repository result")
        void testIsRegisteredAsTrainerTrimsNames() {
            when(trainerRepository.existsByUserFirstNameIgnoreCaseAndUserLastNameIgnoreCase("Jane", "Smith"))
                    .thenReturn(true);

            boolean result = trainerService.isRegisteredAsTrainer("  Jane  ", "  Smith  ");

            assertTrue(result);
            verify(trainerRepository)
                    .existsByUserFirstNameIgnoreCaseAndUserLastNameIgnoreCase("Jane", "Smith");
        }
    }
}
