package com.epam.gymcrmspringboot.mapper;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.CreateUserRequest;
import com.epam.gymcrmspringboot.dto.response.UserResponse;
import com.epam.gymcrmspringboot.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    @DisplayName("Should map CreateTraineeRequest to CreateUserRequest")
    void testMapCreateTraineeRequestToCreateUserRequest() {
        // Arrange
        CreateTraineeRequest request = new CreateTraineeRequest(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123 Main St"
        );

        // Act
        CreateUserRequest result = userMapper.toCreateUserRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    @Test
    @DisplayName("Should map CreateTrainerRequest to CreateUserRequest")
    void testMapCreateTrainerRequestToCreateUserRequest() {
        // Arrange
        CreateTrainerRequest request = new CreateTrainerRequest(
                "Jane",
                "Smith",
                "Yoga"
        );

        // Act
        CreateUserRequest result = userMapper.toCreateUserRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
    }

    @Test
    @DisplayName("Should handle null CreateTraineeRequest")
    void testMapNullCreateTraineeRequest() {
        // Act
        CreateUserRequest result = userMapper.toCreateUserRequest((CreateTraineeRequest) null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null CreateTrainerRequest")
    void testMapNullCreateTrainerRequest() {
        // Act
        CreateUserRequest result = userMapper.toCreateUserRequest((CreateTrainerRequest) null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should preserve whitespace in names during mapping")
    void testMapPreservesNames() {
        // Arrange
        CreateTraineeRequest request = new CreateTraineeRequest(
                "  John  ",
                "  Doe  ",
                LocalDate.of(1990, 1, 1),
                "123 Main St"
        );

        // Act
        CreateUserRequest result = userMapper.toCreateUserRequest(request);

        // Assert
        assertNotNull(result);
        assertEquals("  John  ", result.getFirstName());
        assertEquals("  Doe  ", result.getLastName());
    }

    @Test
    @DisplayName("Should map UserEntity to UserResponse with firstName lastName and username")
    void testMapUserEntityToUserResponse() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .firstName("Alice")
                .lastName("Walker")
                .username("alice.walker")
                .password("hidden")
                .isActive(true)
                .build();

        // Act
        UserResponse response = userMapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals("Alice", response.getFirstName());
        assertEquals("Walker", response.getLastName());
        assertEquals("alice.walker", response.getUsername());
    }

    @Test
    @DisplayName("Should return null when UserEntity is null")
    void testMapNullUserEntityToUserResponse() {
        // Act
        UserResponse response = userMapper.toResponse(null);

        // Assert
        assertNull(response);
    }
}

