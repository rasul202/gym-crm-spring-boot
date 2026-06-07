package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("password123")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create and retrieve user by ID")
        void testCreateAndRetrieveUser() {
            // Arrange & Act
            UserEntity saved = userRepository.save(userEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(userRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should update user successfully")
        void testUpdateUser() {
            // Arrange
            UserEntity saved = userRepository.save(userEntity);

            // Act
            saved.setFirstName("Jane");
            UserEntity updated = userRepository.save(saved);

            // Assert
            assertEquals("Jane", updated.getFirstName());
        }

        @Test
        @DisplayName("Should delete user successfully")
        void testDeleteUser() {
            // Arrange
            UserEntity saved = userRepository.save(userEntity);

            // Act
            userRepository.delete(saved);

            // Assert
            assertFalse(userRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should retrieve all users")
        void testFindAllUsers() {
            // Arrange
            userRepository.save(userEntity);
            UserEntity anotherUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("Jane.Smith")
                    .password("password456")
                    .build();
            userRepository.save(anotherUser);

            // Act
            var allUsers = userRepository.findAll();

            // Assert
            assertEquals(2, allUsers.size());
        }
    }

    @Nested
    @DisplayName("findByUsername Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should find user by username")
        void testFindByUsername() {
            // Arrange
            userRepository.save(userEntity);

            // Act
            Optional<UserEntity> result = userRepository.findByUsername("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John.Doe", result.get().getUsername());
        }

        @Test
        @DisplayName("Should return empty Optional when user not found")
        void testFindByUsernameNotFound() {
            // Act
            Optional<UserEntity> result = userRepository.findByUsername("NonExistent");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find user regardless of active status")
        void testFindByUsernameIgnoresActiveStatus() {
            // Arrange
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            // Act
            Optional<UserEntity> result = userRepository.findByUsername("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            assertFalse(result.get().getIsActive());
        }
    }

    @Nested
    @DisplayName("findByUsernameAndIsActiveTrue Tests")
    class FindByUsernameAndIsActiveTrueTests {

        @Test
        @DisplayName("Should find active user by username")
        void testFindActiveUserByUsername() {
            // Arrange
            userRepository.save(userEntity);

            // Act
            Optional<UserEntity> result = userRepository.findByUsernameAndIsActiveTrue("John.Doe");

            // Assert
            assertTrue(result.isPresent());
            assertTrue(result.get().getIsActive());
        }

        @Test
        @DisplayName("Should not find inactive user by username")
        void testDoesNotFindInactiveUserByUsername() {
            // Arrange
            userEntity.setIsActive(false);
            userRepository.save(userEntity);

            // Act
            Optional<UserEntity> result = userRepository.findByUsernameAndIsActiveTrue("John.Doe");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty Optional when user not found")
        void testFindByUsernameAndIsActiveTrueNotFound() {
            // Act
            Optional<UserEntity> result = userRepository.findByUsernameAndIsActiveTrue("NonExistent");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find only active users from multiple users")
        void testFindOnlyActiveUsers() {
            // Arrange
            userRepository.save(userEntity);

            UserEntity inactiveUser = UserEntity.builder()
                    .firstName("John")
                    .lastName("Inactive")
                    .username("John.Inactive")
                    .password("password789")
                    .isActive(false)
                    .build();
            userRepository.save(inactiveUser);

            // Act
            Optional<UserEntity> activeResult = userRepository.findByUsernameAndIsActiveTrue("John.Doe");
            Optional<UserEntity> inactiveResult = userRepository.findByUsernameAndIsActiveTrue("John.Inactive");

            // Assert
            assertTrue(activeResult.isPresent());
            assertFalse(inactiveResult.isPresent());
        }
    }

    @Nested
    @DisplayName("Username Uniqueness Tests")
    class UsernameUniquenessTests {

        @Test
        @DisplayName("Should enforce unique username constraint")
        void testUsernameUniquenessConstraint() {
            // Arrange
            userRepository.save(userEntity);

            UserEntity duplicateUser = UserEntity.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .username("John.Doe")
                    .password("password999")
                    .build();

            // Act & Assert
            assertThrows(Exception.class, () -> userRepository.save(duplicateUser));
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should set isActive to true by default")
        void testDefaultIsActiveValue() {
            // Arrange & Act
            UserEntity saved = userRepository.save(userEntity);

            // Assert
            assertTrue(saved.getIsActive());
        }
    }
}

