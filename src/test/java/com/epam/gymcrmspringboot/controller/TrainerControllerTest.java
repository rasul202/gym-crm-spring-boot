package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.TrainerService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link TrainerController}.
 * Uses standaloneSetup (plain-Spring equivalent of @WebMvcTest) — no full context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController Tests")
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // POST /trainers
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /trainers")
    class RegisterTrainerTests {

        @Test
        @DisplayName("Should return 201 with credentials when trainer is registered successfully")
        void shouldReturn201WhenTrainerIsRegisteredSuccessfully() throws Exception {
            // Arrange
            CreateTrainerRequest request = new CreateTrainerRequest("Jane", "Smith", "Yoga");
            RegistrationResponse response = new RegistrationResponse("Jane.Smith", "generatedPass");
            when(trainerService.registerTrainer(any(CreateTrainerRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("Jane.Smith"))
                    .andExpect(jsonPath("$.password").value("generatedPass"));

            verify(trainerService).registerTrainer(any(CreateTrainerRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when firstName is blank")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {
            // Arrange
            CreateTrainerRequest request = new CreateTrainerRequest("", "Smith", "Yoga");

            // Act & Assert
            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.firstName").exists());

            verify(trainerService, never()).registerTrainer(any());
        }

        @Test
        @DisplayName("Should return 400 when lastName is blank")
        void shouldReturn400WhenLastNameIsBlank() throws Exception {
            // Arrange
            CreateTrainerRequest request = new CreateTrainerRequest("Jane", "", "Yoga");

            // Act & Assert
            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.lastName").exists());
        }

        @Test
        @DisplayName("Should return 400 when specialization is blank")
        void shouldReturn400WhenSpecializationIsBlank() throws Exception {
            // Arrange
            CreateTrainerRequest request = new CreateTrainerRequest("Jane", "Smith", "");

            // Act & Assert
            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.specialization").exists());
        }

        @Test
        @DisplayName("Should return 400 when request body is missing")
        void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 409 when user is already registered as trainee")
        void shouldReturn409WhenUserAlreadyRegisteredAsTrainee() throws Exception {
            CreateTrainerRequest request = new CreateTrainerRequest("Jane", "Smith", "Yoga");
            when(trainerService.registerTrainer(any(CreateTrainerRequest.class)))
                    .thenThrow(new UserAlreadyRegisteredInOppositeRoleException("Cannot register as trainer: user is already registered as trainee"));

            mockMvc.perform(post("/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot register as trainer: user is already registered as trainee"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /trainers/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /trainers/{username}")
    class GetTrainerProfileTests {

        @Test
        @DisplayName("Should return 200 with profile when trainer is found")
        void shouldReturn200WhenTrainerIsFound() throws Exception {
            // Arrange
            GetTrainerProfileResponse profile = new GetTrainerProfileResponse(
                    "Jane", "Smith", "Yoga", true,
                    List.of(new TraineeSummary("trainee.one", "Trainee", "One")));
            when(trainerService.getTrainerByUsername(eq("Jane.Smith"), any())).thenReturn(profile);

            // Act & Assert
            mockMvc.perform(get("/trainers/Jane.Smith"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.lastName").value("Smith"))
                    .andExpect(jsonPath("$.specialization").value("Yoga"))
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.trainees[0].username").value("trainee.one"));

            verify(trainerService).getTrainerByUsername(eq("Jane.Smith"), any());
        }

        @Test
        @DisplayName("Should return 404 when trainer is not found")
        void shouldReturn404WhenTrainerIsNotFound() throws Exception {
            // Arrange
            when(trainerService.getTrainerByUsername(eq("Unknown"), any()))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));

            // Act & Assert
            mockMvc.perform(get("/trainers/Unknown"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainer not found"));
        }

        @Test
        @DisplayName("Should return 401 when service throws AuthenticationException")
        void shouldReturn401WhenAuthenticationFails() throws Exception {
            // Arrange
            when(trainerService.getTrainerByUsername(eq("Jane.Smith"), any()))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            // Act & Assert
            mockMvc.perform(get("/trainers/Jane.Smith"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /trainers/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /trainers/{username}")
    class UpdateTrainerProfileTests {

        @Test
        @DisplayName("Should return 200 with updated profile when update is successful")
        void shouldReturn200WhenTrainerIsUpdatedSuccessfully() throws Exception {
            // Arrange
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("Jane", "Smith", "Pilates", true);
            UpdateTrainerProfileResponse response = new UpdateTrainerProfileResponse(
                    "Jane.Smith", "Jane", "Smith", "Pilates", true, List.of());
            when(trainerService.updateTrainer(eq("Jane.Smith"), any(), any()))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("Jane.Smith"))
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.specialization").value("Pilates"))
                    .andExpect(jsonPath("$.isActive").value(true));

            verify(trainerService).updateTrainer(eq("Jane.Smith"), any(), any());
        }

        @Test
        @DisplayName("Should return 400 when firstName is blank")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {
            // Arrange
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("", "Smith", "Yoga", true);

            // Act & Assert
            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.firstName").exists());
        }

        @Test
        @DisplayName("Should return 400 when lastName is blank")
        void shouldReturn400WhenLastNameIsBlank() throws Exception {
            // Arrange
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("Jane", "", "Yoga", true);

            // Act & Assert
            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.lastName").exists());
        }

        @Test
        @DisplayName("Should return 400 when isActive is null")
        void shouldReturn400WhenIsActiveIsNull() throws Exception {
            // Arrange
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("Jane", "Smith", "Yoga", null);

            // Act & Assert
            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.isActive").exists());
        }

        @Test
        @DisplayName("Should return 401 when update authentication fails")
        void shouldReturn401WhenUpdateAuthenticationFails() throws Exception {
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("Jane", "Smith", "Yoga", true);
            when(trainerService.updateTrainer(eq("Jane.Smith"), any(), any()))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should return 404 when update target trainer is missing")
        void shouldReturn404WhenUpdateTargetMissing() throws Exception {
            UpdateTrainerProfileRequest request =
                    new UpdateTrainerProfileRequest("Jane", "Smith", "Yoga", true);
            when(trainerService.updateTrainer(eq("Jane.Smith"), any(), any()))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));

            mockMvc.perform(put("/trainers/Jane.Smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainer not found"));
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /trainers/{username}/status
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /trainers/{username}/status")
    class ActivateDeactivateTrainerTests {

        @Test
        @DisplayName("Should return 200 when trainer is activated successfully")
        void shouldReturn200WhenTrainerIsActivated() throws Exception {
            // Arrange
            doNothing().when(trainerService).activateTrainer(eq("Jane.Smith"), any());

            // Act & Assert
            mockMvc.perform(patch("/trainers/Jane.Smith/status")
                            .param("isActive", "true"))
                    .andExpect(status().isOk());

            verify(trainerService).activateTrainer(eq("Jane.Smith"), any());
            verify(trainerService, never()).deactivateTrainer(any(), any());
        }

        @Test
        @DisplayName("Should return 200 when trainer is deactivated successfully")
        void shouldReturn200WhenTrainerIsDeactivated() throws Exception {
            // Arrange
            doNothing().when(trainerService).deactivateTrainer(eq("Jane.Smith"), any());

            // Act & Assert
            mockMvc.perform(patch("/trainers/Jane.Smith/status")
                            .param("isActive", "false"))
                    .andExpect(status().isOk());

            verify(trainerService).deactivateTrainer(eq("Jane.Smith"), any());
            verify(trainerService, never()).activateTrainer(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when isActive param is missing")
        void shouldReturn400WhenIsActiveParamIsMissing() throws Exception {
            mockMvc.perform(patch("/trainers/Jane.Smith/status"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when authentication fails during status change")
        void shouldReturn401WhenAuthenticationFails() throws Exception {
            // Arrange
            doThrow(new AuthenticationException("Invalid credentials"))
                    .when(trainerService).activateTrainer(eq("Jane.Smith"), any());

            // Act & Assert
            mockMvc.perform(patch("/trainers/Jane.Smith/status")
                            .param("isActive", "true"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }
    }
}
