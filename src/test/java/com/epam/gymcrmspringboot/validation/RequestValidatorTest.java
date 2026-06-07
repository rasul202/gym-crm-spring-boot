package com.epam.gymcrmspringboot.validation;

import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("RequestValidator Tests")
@ExtendWith(MockitoExtension.class)
class RequestValidatorTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private RequestValidator requestValidator;

    @Test
    @DisplayName("Should throw IllegalArgumentException when validation violations exist")
    void testValidateThrowsExceptionForInvalidRequest() {
        // Arrange
        LoginRequest request = new LoginRequest("", "");

        @SuppressWarnings("unchecked")
        ConstraintViolation<LoginRequest> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("error message");

        Set<ConstraintViolation<LoginRequest>> violations = Set.of(violation);
        org.mockito.Mockito.doReturn(violations).when(validator).validate(any());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> requestValidator.validate(request));
        assertTrue(exception.getMessage().contains("error message"));
    }

    @Test
    @DisplayName("Should handle requests without violations")
    void testValidateHandlesEmptyViolations() {
        // Arrange
        LoginRequest request = new LoginRequest("validuser", "password");
        when(validator.validate(request)).thenReturn(new HashSet<>());

        // Act & Assert
        assertDoesNotThrow(() -> requestValidator.validate(request));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when request is null")
    void testValidateThrowsExceptionForNullRequest() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requestValidator.validate(null));
        assertEquals("request must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should join and sort multiple violation messages")
    void testValidateSortsAndJoinsViolationMessages() {
        // Arrange
        LoginRequest request = new LoginRequest("", "");

        @SuppressWarnings("unchecked")
        ConstraintViolation<LoginRequest> violationZ = org.mockito.Mockito.mock(ConstraintViolation.class);
        @SuppressWarnings("unchecked")
        ConstraintViolation<LoginRequest> violationA = org.mockito.Mockito.mock(ConstraintViolation.class);

        when(violationZ.getMessage()).thenReturn("z-message");
        when(violationA.getMessage()).thenReturn("a-message");

        Set<ConstraintViolation<LoginRequest>> violations = Set.of(violationZ, violationA);
        org.mockito.Mockito.doReturn(violations).when(validator).validate(any());

        // Act
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> requestValidator.validate(request));

        // Assert
        assertEquals("a-message; z-message", exception.getMessage());
    }
}

