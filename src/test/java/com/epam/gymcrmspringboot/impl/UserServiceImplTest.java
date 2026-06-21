package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.response.CreateUserProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.SamePasswordException;
import com.epam.gymcrmspringboot.model.UserEntity;
import com.epam.gymcrmspringboot.repository.UserRepository;
import com.epam.gymcrmspringboot.service.AuthenticationService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private UserEntity userEntity;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authentication = mock(Authentication.class);

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
    }

    @Nested
    @DisplayName("createUserProfile Tests")
    class CreateUserProfileTests {

        @Test
        @DisplayName("Should create user profile successfully")
        void testCreateUserProfileSuccess() {
            // Arrange
            when(userRepository.existsByUsername("John.Doe")).thenReturn(false);
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
            when(userRepository.existsByUsername("John.Doe")).thenReturn(false);
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

            when(userRepository.existsByUsername("John.Doe")).thenReturn(false);
            when(usernamePasswordUtil.generatePassword()).thenReturn("RawPass123");
            when(passwordEncoder.encode("RawPass123")).thenReturn("encodedPass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            userService.createUserProfile(createUserRequest);

            // Assert
            verify(userRepository).existsByUsername("John.Doe");
        }

        @Test
        @DisplayName("Should append numeric suffix when base username already exists")
        void testCreateUserProfileAddsNumericSuffix() {
            // Arrange
            UserEntity savedUser = UserEntity.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .username("John.Doe2")
                    .password("encodedPass")
                    .isActive(true)
                    .build();

            when(userRepository.existsByUsername("John.Doe")).thenReturn(true);
            when(userRepository.existsByUsername("John.Doe1")).thenReturn(true);
            when(userRepository.existsByUsername("John.Doe2")).thenReturn(false);
            when(usernamePasswordUtil.generatePassword()).thenReturn("RawPass123");
            when(passwordEncoder.encode("RawPass123")).thenReturn("encodedPass");
            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

            // Act
            CreateUserProfileResponse result = userService.createUserProfile(createUserRequest);

            // Assert
            assertEquals("John.Doe2", result.getUsername());
            verify(userRepository).existsByUsername("John.Doe");
            verify(userRepository).existsByUsername("John.Doe1");
            verify(userRepository).existsByUsername("John.Doe2");
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
            // Arrange: old="TestPass123", new="NewPassword123" — different strings
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "TestPass123", "NewPassword123");

            doNothing().when(authenticationService).assertAuthenticatedUser(eq("John.Doe"), any());
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity)); // userEntity.password = "TestPass123"
            when(passwordEncoder.matches("TestPass123", "TestPass123")).thenReturn(true);
            when(passwordEncoder.encode("NewPassword123")).thenReturn("encodedNew");
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

            // Act
            userService.changePassword(request, authentication);

            // Assert
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw SamePasswordException when new password equals old password")
        void testChangePasswordThrowsExceptionForSamePassword() {
            // Arrange: same string for old and new
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "TestPass123", "TestPass123");

            doNothing().when(authenticationService).assertAuthenticatedUser(eq("John.Doe"), any());

            // Act & Assert — impl uses plain string equality check before touching the encoder
            assertThrows(SamePasswordException.class,
                    () -> userService.changePassword(request, authentication));
        }

        @Test
        @DisplayName("Should throw AuthenticationException when old password does not match stored password")
        void testChangePasswordThrowsExceptionWhenOldPasswordDoesNotMatch() {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "WrongPass", "NewPassword123");

            doNothing().when(authenticationService).assertAuthenticatedUser(eq("John.Doe"), any());
            when(userRepository.findByUsernameAndIsActiveTrue("John.Doe"))
                    .thenReturn(Optional.of(userEntity));
            when(passwordEncoder.matches("WrongPass", "TestPass123")).thenReturn(false);

            // Act & Assert
            assertThrows(AuthenticationException.class,
                    () -> userService.changePassword(request, authentication));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void testChangePasswordThrowsExceptionWhenUserNotFound() {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("nonexistent", "OldPass", "NewPass");

            doNothing().when(authenticationService).assertAuthenticatedUser(eq("nonexistent"), any());
            when(userRepository.findByUsernameAndIsActiveTrue("nonexistent"))
                    .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotFoundException.class,
                    () -> userService.changePassword(request, authentication));
        }
    }
}
