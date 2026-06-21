package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController}.
 * Uses standaloneSetup (plain-Spring equivalent of @WebMvcTest) — no full context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "jwtCookieName", "JWT_TOKEN");
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // POST /authentication/login
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /authentication/login")
    class LoginTests {

        @Test
        @DisplayName("Should return 204 with Set-Cookie header when credentials are valid")
        void shouldReturn204WhenCredentialsAreValid() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("John.Doe", "secret123");
            when(authenticationService.authenticate("John.Doe", "secret123")).thenReturn("jwt-token-value");

            // Act & Assert
            mockMvc.perform(post("/authentication/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andExpect(header().exists("Set-Cookie"));

            verify(authenticationService).authenticate("John.Doe", "secret123");
        }

        @Test
        @DisplayName("Should return 400 when username is blank")
        void shouldReturn400WhenUsernameIsBlank() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("", "secret123");

            // Act & Assert
            mockMvc.perform(post("/authentication/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).authenticate(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when password is blank")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("John.Doe", "");

            // Act & Assert
            mockMvc.perform(post("/authentication/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).authenticate(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when request body is missing")
        void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/authentication/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(authenticationService, never()).authenticate(any(), any());
        }

        @Test
        @DisplayName("Should return 401 when authentication service rejects credentials")
        void shouldReturn401WhenAuthenticationServiceRejectsCredentials() throws Exception {
            LoginRequest request = new LoginRequest("John.Doe", "wrong");
            when(authenticationService.authenticate("John.Doe", "wrong"))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            mockMvc.perform(post("/authentication/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /authentication/logout
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /authentication/logout")
    class LogoutTests {

        @Test
        @DisplayName("Should return 204 with cleared cookie header on logout")
        void shouldReturn204WithClearedCookieOnLogout() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/authentication/logout"))
                    .andExpect(status().isNoContent())
                    .andExpect(header().exists("Set-Cookie"));

            verifyNoInteractions(authenticationService);
        }
    }
}
