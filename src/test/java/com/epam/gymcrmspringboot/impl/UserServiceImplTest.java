package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.SamePasswordException;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.impl.UserServiceImpl;
import com.epam.gymcrmspringboot.util.UsernamePasswordUtil;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("UserServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernamePasswordUtil usernamePasswordUtil;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private UserEntity userEntity;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        userEntity = UserEntity.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("TestPass123")
                .isActive(true)
                .build();

        loginRequest = LoginRequest.builder()
                .username("John.Doe")
                .password("TestPass123")
                .build();
    }

    @Nested
    @DisplayName("createUserProfile Tests")
    class CreateUserProfileTests {

        @Test
        @DisplayName("Should create user profile successfully")
        void testCreateUserProfileSuccess() {
            // Arrange
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            when(usernamePasswordUtil.generateUsername(anyString(), anyString(), any()))
                    .thenReturn("John.Doe");
            when(usernamePasswordUtil.generatePassword()).thenReturn("RawPass123");
            when(passwordEncoder.encode("RawPass123")).thenReturn("encodedPass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            CreateUserProfileResponse result = userService.createUserProfile(createUserRequest);

            // Assert
            assertNotNull(result);
            assertEquals("John.Doe", result.getUsername());
            assertEquals("RawPass123", result.getPassword());
            assertEquals(1L, result.getId());
            verify(userRepository).save(any(UserEntity.class));
            verify(requestValidator).validate(createUserRequest);
        }

        @Test
        @DisplayName("Should validate request before creating user")
        void testCreateUserProfileValidates() {
            // Arrange
            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            when(usernamePasswordUtil.generateUsername(anyString(), anyString(), any()))
                    .thenReturn("John.Doe");
            when(usernamePasswordUtil.generatePassword()).thenReturn("RawPass123");
            when(passwordEncoder.encode("RawPass123")).thenReturn("encodedPass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            userService.createUserProfile(createUserRequest);

            // Assert
            verify(requestValidator).validate(createUserRequest);
        }

        @Test
        @DisplayName("Should trim first and last name before username generation")
        void testCreateUserProfileTrimsNames() {
            // Arrange
            createUserRequest.setFirstName("  John  ");
            createUserRequest.setLastName("  Doe  ");

            when(userRepository.findAll()).thenReturn(Collections.emptyList());
            when(usernamePasswordUtil.generateUsername(eq("John"), eq("Doe"), any()))
                    .thenReturn("John.Doe");
            when(usernamePasswordUtil.generatePassword()).thenReturn("RawPass123");
            when(passwordEncoder.encode("RawPass123")).thenReturn("encodedPass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            userService.createUserProfile(createUserRequest);

            // Assert
            verify(usernamePasswordUtil).generateUsername(eq("John"), eq("Doe"), any());
        }
    }

    @Nested
    @DisplayName("authenticateActiveUser Tests")
    class AuthenticateActiveUserTests {

        @Test
        @DisplayName("Should authenticate active user with correct password")
        void testAuthenticateActiveUserSuccess() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("TestPass123", "TestPass123")).thenReturn(true);

            // Act
            boolean result = userService.authenticateActiveUser(loginRequest);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void testAuthenticateActiveUserThrowsExceptionForNonexistentUser() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            LoginRequest invalidRequest = LoginRequest.builder()
                    .username("nonexistent")
                    .password("password")
                    .build();

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> userService.authenticateActiveUser(invalidRequest));
        }

        @Test
        @DisplayName("Should return false when password is incorrect")
        void testAuthenticateActiveUserFailsWithWrongPassword() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("WrongPassword", "TestPass123")).thenReturn(false);

            LoginRequest invalidRequest = LoginRequest.builder()
                    .username("John.Doe")
                    .password("WrongPassword")
                    .build();

            // Act
            boolean result = userService.authenticateActiveUser(invalidRequest);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should validate login request")
        void testAuthenticateActiveUserValidatesRequest() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("TestPass123", "TestPass123")).thenReturn(true);

            // Act
            userService.authenticateActiveUser(loginRequest);

            // Assert
            verify(requestValidator).validate(loginRequest);
        }
    }

    @Nested
    @DisplayName("authenticateAnyUser Tests")
    class AuthenticateAnyUserTests {

        @Test
        @DisplayName("Should authenticate inactive user when credentials are correct")
        void testAuthenticateAnyUserSuccess() {
            // Arrange
            UserEntity inactiveUser = UserEntity.builder()
                    .username("John.Doe")
                    .password("encoded")
                    .isActive(false)
                    .build();

            when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(inactiveUser));
            when(passwordEncoder.matches("TestPass123", "encoded")).thenReturn(true);

            // Act
            boolean result = userService.authenticateAnyUser(loginRequest);

            // Assert
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("deactivateUserProfile Tests")
    class DeactivateUserProfileTests {

        @Test
        @DisplayName("Should deactivate active user successfully")
        void testDeactivateUserProfileSuccess() {
            // Arrange
            UserEntity activeUser = UserEntity.builder()
                    .id(1L)
                    .username("John.Doe")
                    .isActive(true)
                    .build();

            when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(activeUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(activeUser);

            // Act
            boolean result = userService.deactivateUserProfile("John.Doe");

            // Assert
            assertTrue(result);
            assertFalse(activeUser.getIsActive());
            verify(userRepository).save(activeUser);
        }

        @Test
        @DisplayName("Should return false when user already inactive")
        void testDeactivateUserProfileReturnsFalseWhenAlreadyInactive() {
            // Arrange
            UserEntity inactiveUser = UserEntity.builder()
                    .id(1L)
                    .username("John.Doe")
                    .isActive(false)
                    .build();

            when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(inactiveUser));

            // Act
            boolean result = userService.deactivateUserProfile("John.Doe");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void testDeactivateUserProfileThrowsException() {
            // Arrange
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> userService.deactivateUserProfile("nonexistent"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when username is blank")
        void testDeactivateUserProfileThrowsExceptionForBlankUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.deactivateUserProfile("   "));
            assertThrows(IllegalArgumentException.class,
                    () -> userService.deactivateUserProfile(null));
        }
    }

    @Nested
    @DisplayName("activateUserProfile Tests")
    class ActivateUserProfileTests {

        @Test
        @DisplayName("Should activate inactive user successfully")
        void testActivateUserProfileSuccess() {
            // Arrange
            UserEntity inactiveUser = UserEntity.builder()
                    .id(1L)
                    .username("John.Doe")
                    .isActive(false)
                    .build();

            when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(inactiveUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(inactiveUser);

            // Act
            boolean result = userService.activateUserProfile("John.Doe");

            // Assert
            assertTrue(result);
            assertTrue(inactiveUser.getIsActive());
            verify(userRepository).save(inactiveUser);
        }

        @Test
        @DisplayName("Should return false when user already active")
        void testActivateUserProfileReturnsFalseWhenAlreadyActive() {
            // Arrange
            UserEntity activeUser = UserEntity.builder()
                    .id(1L)
                    .username("John.Doe")
                    .isActive(true)
                    .build();

            when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(activeUser));

            // Act
            boolean result = userService.activateUserProfile("John.Doe");

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void testActivateUserProfileThrowsException() {
            // Arrange
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> userService.activateUserProfile("nonexistent"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when username is blank")
        void testActivateUserProfileThrowsExceptionForBlankUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.activateUserProfile("   "));
            assertThrows(IllegalArgumentException.class,
                    () -> userService.activateUserProfile(null));
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void testChangePasswordSuccess() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("NewPassword123", "TestPass123")).thenReturn(false);
            when(passwordEncoder.encode("NewPassword123")).thenReturn("encodedNew");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            userService.changePassword("John.Doe", "NewPassword123");

            // Assert
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw SamePasswordException when new password equals old password")
        void testChangePasswordThrowsExceptionForSamePassword() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("TestPass123", "TestPass123")).thenReturn(true);

            // Act & Assert
            assertThrows(SamePasswordException.class,
                    () -> userService.changePassword("John.Doe", "TestPass123"));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void testChangePasswordThrowsException() {
            // Arrange
            when(userRepository.findByUsernameAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> userService.changePassword("nonexistent", "newpass"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when username is blank")
        void testChangePasswordThrowsExceptionForBlankUsername() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword("   ", "newpass"));
            assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword(null, "newpass"));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when newPassword is blank")
        void testChangePasswordThrowsExceptionForBlankPassword() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword("John.Doe", "   "));
            assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword("John.Doe", null));
        }
    }
}


