package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeTrainersListRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.exception.UserAlreadyRegisteredInOppositeRoleException;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.TraineeService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link TraineeController}.
 * Uses standaloneSetup (plain-Spring equivalent of @WebMvcTest) — no full context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeController Tests")
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TraineeController traineeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // POST /trainees
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /trainees")
    class RegisterTraineeTests {

        @Test
        @DisplayName("Should return 201 with credentials when trainee is registered successfully")
        void shouldReturn201WhenTraineeIsRegisteredSuccessfully() throws Exception {
            // Arrange
            CreateTraineeRequest request =
                    new CreateTraineeRequest("John", "Doe", LocalDate.of(1990, 5, 20), "123 Main St");
            RegistrationResponse response = new RegistrationResponse("John.Doe", "generatedPass1");
            when(traineeService.registerTrainee(any(CreateTraineeRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("John.Doe"))
                    .andExpect(jsonPath("$.password").value("generatedPass1"));

            verify(traineeService).registerTrainee(any(CreateTraineeRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when firstName is blank")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {
            // Arrange
            CreateTraineeRequest request =
                    new CreateTraineeRequest("", "Doe", LocalDate.of(1990, 5, 20), "address");

            // Act & Assert
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.firstName").exists());

            verify(traineeService, never()).registerTrainee(any());
        }

        @Test
        @DisplayName("Should return 400 when lastName is blank")
        void shouldReturn400WhenLastNameIsBlank() throws Exception {
            // Arrange
            CreateTraineeRequest request =
                    new CreateTraineeRequest("John", "", LocalDate.of(1990, 5, 20), "address");

            // Act & Assert
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.lastName").exists());
        }

        @Test
        @DisplayName("Should return 400 when dateOfBirth is in the future")
        void shouldReturn400WhenDateOfBirthIsInFuture() throws Exception {
            // Arrange
            CreateTraineeRequest request =
                    new CreateTraineeRequest("John", "Doe", LocalDate.of(2099, 1, 1), "address");

            // Act & Assert
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
        }

        @Test
        @DisplayName("Should return 201 when dateOfBirth and address are omitted (optional fields)")
        void shouldReturn201WhenOptionalFieldsAreOmitted() throws Exception {
            // Arrange
            CreateTraineeRequest request =
                    new CreateTraineeRequest("Jane", "Smith", null, null);
            RegistrationResponse response = new RegistrationResponse("Jane.Smith", "pass");
            when(traineeService.registerTrainee(any())).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("Jane.Smith"));
        }

        @Test
        @DisplayName("Should return 409 when user is already registered as trainer")
        void shouldReturn409WhenUserAlreadyRegisteredAsTrainer() throws Exception {
            CreateTraineeRequest request =
                    new CreateTraineeRequest("John", "Doe", LocalDate.of(1990, 5, 20), "123 Main St");
            when(traineeService.registerTrainee(any(CreateTraineeRequest.class)))
                    .thenThrow(new UserAlreadyRegisteredInOppositeRoleException("Cannot register as trainee: user is already registered as trainer"));

            mockMvc.perform(post("/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cannot register as trainee: user is already registered as trainer"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /trainees/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /trainees/{username}")
    class GetTraineeProfileTests {

        @Test
        @DisplayName("Should return 200 with profile when trainee is found")
        void shouldReturn200WhenTraineeIsFound() throws Exception {
            // Arrange
            GetTraineeProfileResponse profile = new GetTraineeProfileResponse(
                    "John", "Doe", LocalDate.of(1990, 5, 20), "123 Main St", true,
                    List.of(new TrainerSummary("trainer.one", "Trainer", "One", "Yoga")));
            when(traineeService.getTraineeByUsername("John.Doe", "secret")).thenReturn(profile);

            // Act & Assert
            mockMvc.perform(get("/trainees/John.Doe")
                            .header("Password", "secret"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.isActive").value(true))
                    .andExpect(jsonPath("$.trainers[0].username").value("trainer.one"));

            verify(traineeService).getTraineeByUsername("John.Doe", "secret");
        }

        @Test
        @DisplayName("Should return 404 when trainee is not found")
        void shouldReturn404WhenTraineeIsNotFound() throws Exception {
            // Arrange
            when(traineeService.getTraineeByUsername("Unknown", "pass"))
                    .thenThrow(new EntityNotFoundException("Trainee not found"));

            // Act & Assert
            mockMvc.perform(get("/trainees/Unknown")
                            .header("Password", "pass"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainee not found"));
        }

        @Test
        @DisplayName("Should return 401 when password is wrong")
        void shouldReturn401WhenPasswordIsWrong() throws Exception {
            // Arrange
            when(traineeService.getTraineeByUsername("John.Doe", "wrongPass"))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            // Act & Assert
            mockMvc.perform(get("/trainees/John.Doe")
                            .header("Password", "wrongPass"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should return 400 when Password header is missing")
        void shouldReturn400WhenPasswordHeaderIsMissing() throws Exception {
            mockMvc.perform(get("/trainees/John.Doe"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /trainees/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /trainees/{username}")
    class UpdateTraineeProfileTests {

        @Test
        @DisplayName("Should return 200 with updated profile when update is successful")
        void shouldReturn200WhenTraineeIsUpdatedSuccessfully() throws Exception {
            // Arrange
            UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                    "John", "Doe", LocalDate.of(1990, 5, 20), "456 Oak Ave", true);
            UpdateTraineeProfileResponse response = new UpdateTraineeProfileResponse(
                    "John.Doe", "John", "Doe", LocalDate.of(1990, 5, 20), "456 Oak Ave", true, List.of());
            when(traineeService.updateTrainee(eq("John.Doe"), eq("secret"), any())).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("John.Doe"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.isActive").value(true));

            verify(traineeService).updateTrainee(eq("John.Doe"), eq("secret"), any());
        }

        @Test
        @DisplayName("Should return 400 when firstName is blank")
        void shouldReturn400WhenFirstNameIsBlank() throws Exception {
            // Arrange
            UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                    "", "Doe", null, null, true);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.firstName").exists());
        }

        @Test
        @DisplayName("Should return 400 when isActive is null")
        void shouldReturn400WhenIsActiveIsNull() throws Exception {
            // Arrange
            UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                    "John", "Doe", null, null, null);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.isActive").exists());
        }

        @Test
        @DisplayName("Should return 400 when dateOfBirth is in the future")
        void shouldReturn400WhenDateOfBirthIsInFuture() throws Exception {
            // Arrange
            UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                    "John", "Doe", LocalDate.of(2099, 1, 1), null, true);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.dateOfBirth").exists());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /trainees/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("DELETE /trainees/{username}")
    class DeleteTraineeTests {

        @Test
        @DisplayName("Should return 200 when trainee is deleted successfully")
        void shouldReturn200WhenTraineeIsDeletedSuccessfully() throws Exception {
            // Arrange
            doNothing().when(traineeService).deleteTrainee("John.Doe", "secret");

            // Act & Assert
            mockMvc.perform(delete("/trainees/John.Doe")
                            .header("Password", "secret"))
                    .andExpect(status().isOk());

            verify(traineeService).deleteTrainee("John.Doe", "secret");
        }

        @Test
        @DisplayName("Should return 404 when trainee does not exist")
        void shouldReturn404WhenTraineeDoesNotExist() throws Exception {
            // Arrange
            doThrow(new EntityNotFoundException("Trainee not found"))
                    .when(traineeService).deleteTrainee("Unknown", "pass");

            // Act & Assert
            mockMvc.perform(delete("/trainees/Unknown")
                            .header("Password", "pass"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainee not found"));
        }

        @Test
        @DisplayName("Should return 400 when Password header is missing")
        void shouldReturn400WhenPasswordHeaderIsMissing() throws Exception {
            mockMvc.perform(delete("/trainees/John.Doe"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /trainees/{username}/status
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PATCH /trainees/{username}/status")
    class ActivateDeactivateTraineeTests {

        @Test
        @DisplayName("Should return 200 when trainee is activated successfully")
        void shouldReturn200WhenTraineeIsActivated() throws Exception {
            // Arrange
            doNothing().when(traineeService).activateTrainee("John.Doe", "secret");

            // Act & Assert
            mockMvc.perform(patch("/trainees/John.Doe/status")
                            .header("Password", "secret")
                            .param("isActive", "true"))
                    .andExpect(status().isOk());

            verify(traineeService).activateTrainee("John.Doe", "secret");
            verify(traineeService, never()).deactivateTrainee(any(), any());
        }

        @Test
        @DisplayName("Should return 200 when trainee is deactivated successfully")
        void shouldReturn200WhenTraineeIsDeactivated() throws Exception {
            // Arrange
            doNothing().when(traineeService).deactivateTrainee("John.Doe", "secret");

            // Act & Assert
            mockMvc.perform(patch("/trainees/John.Doe/status")
                            .header("Password", "secret")
                            .param("isActive", "false"))
                    .andExpect(status().isOk());

            verify(traineeService).deactivateTrainee("John.Doe", "secret");
            verify(traineeService, never()).activateTrainee(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when isActive param is missing")
        void shouldReturn400WhenIsActiveParamIsMissing() throws Exception {
            mockMvc.perform(patch("/trainees/John.Doe/status")
                            .header("Password", "secret"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /trainees/{username}/trainers
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("PUT /trainees/{username}/trainers")
    class UpdateTraineeTrainersTests {

        @Test
        @DisplayName("Should return 200 with updated trainers list when update is successful")
        void shouldReturn200WhenTrainersListIsUpdatedSuccessfully() throws Exception {
            // Arrange
            UpdateTraineeTrainersListRequest request =
                    new UpdateTraineeTrainersListRequest(List.of("trainer.one", "trainer.two"));
            List<TrainerSummary> summaries = List.of(
                    new TrainerSummary("trainer.one", "Trainer", "One", "Yoga"),
                    new TrainerSummary("trainer.two", "Trainer", "Two", "Pilates"));
            when(traineeService.updateTraineeTrainers(eq("John.Doe"), eq("secret"), anyList()))
                    .thenReturn(summaries);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe/trainers")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].username").value("trainer.one"))
                    .andExpect(jsonPath("$[1].username").value("trainer.two"));

            verify(traineeService).updateTraineeTrainers(eq("John.Doe"), eq("secret"), anyList());
        }

        @Test
        @DisplayName("Should return 400 when trainerUsernames list is empty")
        void shouldReturn400WhenTrainersListIsEmpty() throws Exception {
            // Arrange
            UpdateTraineeTrainersListRequest request =
                    new UpdateTraineeTrainersListRequest(List.of());

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe/trainers")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainerUsernames").exists());
        }

        @Test
        @DisplayName("Should return 400 when trainerUsernames is null")
        void shouldReturn400WhenTrainersListIsNull() throws Exception {
            // Arrange
            UpdateTraineeTrainersListRequest request =
                    new UpdateTraineeTrainersListRequest(null);

            // Act & Assert
            mockMvc.perform(put("/trainees/John.Doe/trainers")
                            .header("Password", "secret")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainerUsernames").exists());
        }
    }

    // -------------------------------------------------------------------------
    // GET /trainees/{traineeUsername}/available-trainers
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /trainees/{traineeUsername}/available-trainers")
    class GetAvailableTrainersTests {

        @Test
        @DisplayName("Should return 200 with available trainers list")
        void shouldReturn200WithAvailableTrainers() throws Exception {
            // Arrange
            List<TrainerSummary> trainers = List.of(
                    new TrainerSummary("trainer.a", "Trainer", "A", "Yoga"));
            when(traineeService.getAvailableTrainersForTrainee("John.Doe", "secret"))
                    .thenReturn(trainers);

            // Act & Assert
            mockMvc.perform(get("/trainees/John.Doe/available-trainers")
                            .header("Password", "secret"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].username").value("trainer.a"))
                    .andExpect(jsonPath("$[0].specialization").value("Yoga"));

            verify(traineeService).getAvailableTrainersForTrainee("John.Doe", "secret");
        }

        @Test
        @DisplayName("Should return 200 with empty list when no trainers are available")
        void shouldReturn200WithEmptyListWhenNoTrainersAvailable() throws Exception {
            // Arrange
            when(traineeService.getAvailableTrainersForTrainee("John.Doe", "secret"))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/trainees/John.Doe/available-trainers")
                            .header("Password", "secret"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 401 when authentication fails")
        void shouldReturn401WhenAuthenticationFails() throws Exception {
            // Arrange
            when(traineeService.getAvailableTrainersForTrainee("John.Doe", "bad"))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            // Act & Assert
            mockMvc.perform(get("/trainees/John.Doe/available-trainers")
                            .header("Password", "bad"))
                    .andExpect(status().isUnauthorized());
        }
    }
}

