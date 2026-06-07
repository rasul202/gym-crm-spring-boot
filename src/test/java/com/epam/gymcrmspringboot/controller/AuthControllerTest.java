package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.ChangePasswordRequest;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link AuthController}.
 * Uses standaloneSetup (the plain-Spring equivalent of @WebMvcTest) so that
 * only the controller slice is loaded — no full Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // GET /users/login
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /users/login")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 when credentials are valid")
        void shouldReturn200WhenCredentialsAreValid() throws Exception {
            // Arrange
            when(userService.authenticateActiveUser(any())).thenReturn(true);

            // Act & Assert
            mockMvc.perform(get("/users/login")
                            .param("username", "John.Doe")
                            .param("password", "secret123"))
                    .andExpect(status().isOk());

            verify(userService).authenticateActiveUser(any());
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            // Arrange
            when(userService.authenticateActiveUser(any())).thenReturn(false);

            // Act & Assert
            mockMvc.perform(get("/users/login")
                            .param("username", "John.Doe")
                            .param("password", "wrongpassword"))
                    .andExpect(status().isUnauthorized());

            verify(userService).authenticateActiveUser(any());
        }

        @Test
        @DisplayName("Should return 400 when username param is missing")
        void shouldReturn400WhenUsernameParamIsMissing() throws Exception {
            mockMvc.perform(get("/users/login")
                            .param("password", "secret123"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticateActiveUser(any());
        }

        @Test
        @DisplayName("Should return 400 when password param is missing")
        void shouldReturn400WhenPasswordParamIsMissing() throws Exception {
            mockMvc.perform(get("/users/login")
                            .param("username", "John.Doe"))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).authenticateActiveUser(any());
        }
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
            when(userService.authenticateActiveUser(any())).thenReturn(true);
            doNothing().when(userService).changePassword("John.Doe", "newPass456");

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).authenticateActiveUser(any());
            verify(userService).changePassword("John.Doe", "newPass456");
        }

        @Test
        @DisplayName("Should return 401 when old password is wrong")
        void shouldReturn401WhenOldPasswordIsWrong() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "wrongOldPass", "newPass456");
            when(userService.authenticateActiveUser(any())).thenReturn(false);

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(userService, never()).changePassword(any(), any());
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

            verify(userService, never()).authenticateActiveUser(any());
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
        @DisplayName("Should return 401 when service throws AuthenticationException")
        void shouldReturn401WhenServiceThrowsAuthenticationException() throws Exception {
            // Arrange
            ChangePasswordRequest request =
                    new ChangePasswordRequest("John.Doe", "oldPass", "newPass");
            when(userService.authenticateActiveUser(any()))
                    .thenThrow(new AuthenticationException("User not found or inactive"));

            // Act & Assert
            mockMvc.perform(put("/users/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User not found or inactive"));
        }
    }
}

