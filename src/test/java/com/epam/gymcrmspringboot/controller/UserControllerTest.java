package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.SamePasswordException;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link UserController}.
 * The controller delegates auth entirely to UserService.changePassword(request, authentication).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // PUT /users/password
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /users/password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should return 200 when password is changed successfully")
        void shouldReturn200WhenPasswordIsChangedSuccessfully() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "oldPass123", "newPass456");
            doNothing().when(userService).changePassword(any(), any());

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).changePassword(any(), any());
        }

        @Test
        @DisplayName("Should return 401 when service throws AuthenticationException (wrong old password)")
        void shouldReturn401WhenOldPasswordIsWrong() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "wrongOldPass", "newPass456");
            doThrow(new AuthenticationException("Invalid current password"))
                    .when(userService).changePassword(any(), any());

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when service throws SamePasswordException")
        void shouldReturn400WhenSamePasswordProvided() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "samePass", "samePass");
            doThrow(new SamePasswordException("New password cannot be same as the old password"))
                    .when(userService).changePassword(any(), any());

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("", "oldPass123", "newPass456");

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.username").exists());

            verify(userService, never()).changePassword(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when oldPassword is blank")
        void shouldReturn400WhenOldPasswordIsBlank() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "", "newPass456");

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.oldPassword").exists());
        }

        @Test
        @DisplayName("Should return 400 when newPassword is blank")
        void shouldReturn400WhenNewPasswordIsBlank() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "oldPass123", "");

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.newPassword").exists());
        }

        @Test
        @DisplayName("Should return 400 when request body is missing")
        void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when service throws AuthenticationException (user not found or inactive)")
        void shouldReturn401WhenServiceThrowsAuthenticationException() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "oldPass", "newPass");
            doThrow(new AuthenticationException("User not found or inactive"))
                    .when(userService).changePassword(any(), any());

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User not found or inactive"));
        }

        @Test
        @DisplayName("Should return 404 when service throws EntityNotFoundException")
        void shouldReturn404WhenServiceThrowsEntityNotFoundException() throws Exception {
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "oldPass", "newPass");
            doThrow(new EntityNotFoundException("User not found: John.Doe"))
                    .when(userService).changePassword(any(), any());

            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User not found: John.Doe"));
        }
    }
}

