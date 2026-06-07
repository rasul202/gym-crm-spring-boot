package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.exception.AuthenticationException;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.TrainingService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link TrainingController}.
 * Uses standaloneSetup (plain-Spring equivalent of @WebMvcTest) — no full context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController Tests")
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // POST /trainings
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("POST /trainings")
    class AddTrainingTests {

        @Test
        @DisplayName("Should return 200 when training is added successfully")
        void shouldReturn200WhenTrainingIsAddedSuccessfully() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), 60);
            doNothing().when(trainingService).addTraining(any(AddTrainingRequest.class), eq("trainerPass"));

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).addTraining(any(AddTrainingRequest.class), eq("trainerPass"));
        }

        @Test
        @DisplayName("Should return 400 when traineeUsername is blank")
        void shouldReturn400WhenTraineeUsernameIsBlank() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "", "jane.smith", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), 60);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.traineeUsername").exists());

            verify(trainingService, never()).addTraining(any(), any());
        }

        @Test
        @DisplayName("Should return 400 when trainerUsername is blank")
        void shouldReturn400WhenTrainerUsernameIsBlank() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), 60);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainerUsername").exists());
        }

        @Test
        @DisplayName("Should return 400 when trainingName is blank")
        void shouldReturn400WhenTrainingNameIsBlank() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "",
                    LocalDate.of(2026, 7, 1), 60);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainingName").exists());
        }

        @Test
        @DisplayName("Should return 400 when trainingDate is null")
        void shouldReturn400WhenTrainingDateIsNull() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "Morning Yoga", null, 60);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainingDate").exists());
        }

        @Test
        @DisplayName("Should return 400 when trainingDuration is null")
        void shouldReturn400WhenTrainingDurationIsNull() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), null);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainingDuration").exists());
        }

        @Test
        @DisplayName("Should return 400 when trainingDuration is zero or negative")
        void shouldReturn400WhenTrainingDurationIsNotPositive() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), 0);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.trainingDuration").exists());
        }

        @Test
        @DisplayName("Should return 400 when Password header is missing")
        void shouldReturn400WhenPasswordHeaderIsMissing() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "john.doe", "jane.smith", "Morning Yoga",
                    LocalDate.of(2026, 7, 1), 60);

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when trainee or trainer is not found")
        void shouldReturn404WhenTraineeOrTrainerNotFound() throws Exception {
            // Arrange
            AddTrainingRequest request = new AddTrainingRequest(
                    "unknown.trainee", "unknown.trainer", "Session",
                    LocalDate.of(2026, 7, 1), 45);
            doThrow(new EntityNotFoundException("Trainee not found"))
                    .when(trainingService).addTraining(any(), eq("trainerPass"));

            // Act & Assert
            mockMvc.perform(post("/trainings")
                            .header("Password", "trainerPass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainee not found"));
        }
    }

    // -------------------------------------------------------------------------
    // GET /trainings/trainees/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /trainings/trainees/{username}")
    class GetTraineeTrainingsTests {

        @Test
        @DisplayName("Should return 200 with trainee trainings list")
        void shouldReturn200WithTraineeTrainingsList() throws Exception {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteria =
                    new GetTraineeTrainingsCriteriaRequest(null, null, null, null);
            List<GetTraineeTrainingsResponse> trainings = List.of(
                    new GetTraineeTrainingsResponse(
                            "Morning Yoga", LocalDate.of(2026, 7, 1), "Yoga", 60,
                            "trainer.one", "john.doe"),
                    new GetTraineeTrainingsResponse(
                            "Evening Run", LocalDate.of(2026, 7, 3), "Cardio", 45,
                            "trainer.two", "john.doe"));
            when(trainingService.getTraineeTrainings(eq("john.doe"), eq("pass"), any()))
                    .thenReturn(trainings);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainees/john.doe")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                    .andExpect(jsonPath("$[0].trainingType").value("Yoga"))
                    .andExpect(jsonPath("$[1].trainingName").value("Evening Run"));

            verify(trainingService).getTraineeTrainings(eq("john.doe"), eq("pass"), any());
        }

        @Test
        @DisplayName("Should return 200 with empty list when no trainings found")
        void shouldReturn200WithEmptyListWhenNoTrainingsFound() throws Exception {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteria =
                    new GetTraineeTrainingsCriteriaRequest(null, null, null, null);
            when(trainingService.getTraineeTrainings(eq("john.doe"), eq("pass"), any()))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/trainings/trainees/john.doe")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 200 with filtered results using date range criteria")
        void shouldReturn200WithFilteredResultsUsingDateRange() throws Exception {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteria = new GetTraineeTrainingsCriteriaRequest(
                    LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31), null, "Yoga");
            List<GetTraineeTrainingsResponse> trainings = List.of(
                    new GetTraineeTrainingsResponse(
                            "Yoga Session", LocalDate.of(2026, 6, 15), "Yoga", 60,
                            "trainer.one", "john.doe"));
            when(trainingService.getTraineeTrainings(eq("john.doe"), eq("pass"), any()))
                    .thenReturn(trainings);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainees/john.doe")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].trainingType").value("Yoga"));
        }

        @Test
        @DisplayName("Should return 400 when criteria has invalid date range (fromDate after toDate)")
        void shouldReturn400WhenFromDateIsAfterToDate() throws Exception {
            // Arrange — invalid: from > to
            GetTraineeTrainingsCriteriaRequest criteria = new GetTraineeTrainingsCriteriaRequest(
                    LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 1), null, null);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainees/john.doe")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 when authentication fails")
        void shouldReturn401WhenAuthenticationFails() throws Exception {
            // Arrange
            GetTraineeTrainingsCriteriaRequest criteria =
                    new GetTraineeTrainingsCriteriaRequest(null, null, null, null);
            when(trainingService.getTraineeTrainings(eq("john.doe"), eq("bad"), any()))
                    .thenThrow(new AuthenticationException("Invalid credentials"));

            // Act & Assert
            mockMvc.perform(get("/trainings/trainees/john.doe")
                            .header("Password", "bad")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------------------------------------------------------
    // GET /trainings/trainers/{username}
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /trainings/trainers/{username}")
    class GetTrainerTrainingsTests {

        @Test
        @DisplayName("Should return 200 with trainer trainings list")
        void shouldReturn200WithTrainerTrainingsList() throws Exception {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteria =
                    new GetTrainerTrainingsCriteriaRequest(null, null, null);
            List<GetTrainerTrainingsResponse> trainings = List.of(
                    new GetTrainerTrainingsResponse(
                            "Morning Yoga", LocalDate.of(2026, 7, 1), "Yoga", 60,
                            "jane.smith", "john.doe"),
                    new GetTrainerTrainingsResponse(
                            "Advanced Pilates", LocalDate.of(2026, 7, 5), "Pilates", 90,
                            "jane.smith", "alice.jones"));
            when(trainingService.getTrainerTrainings(eq("jane.smith"), eq("pass"), any()))
                    .thenReturn(trainings);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainers/jane.smith")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                    .andExpect(jsonPath("$[1].trainingName").value("Advanced Pilates"));

            verify(trainingService).getTrainerTrainings(eq("jane.smith"), eq("pass"), any());
        }

        @Test
        @DisplayName("Should return 200 with empty list when no trainings found")
        void shouldReturn200WithEmptyListWhenNoTrainingsFound() throws Exception {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteria =
                    new GetTrainerTrainingsCriteriaRequest(null, null, null);
            when(trainingService.getTrainerTrainings(eq("jane.smith"), eq("pass"), any()))
                    .thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/trainings/trainers/jane.smith")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should return 400 when criteria has invalid date range")
        void shouldReturn400WhenFromDateIsAfterToDate() throws Exception {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteria = new GetTrainerTrainingsCriteriaRequest(
                    LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 1), null);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainers/jane.smith")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when trainer is not found")
        void shouldReturn404WhenTrainerIsNotFound() throws Exception {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteria =
                    new GetTrainerTrainingsCriteriaRequest(null, null, null);
            when(trainingService.getTrainerTrainings(eq("unknown.trainer"), eq("pass"), any()))
                    .thenThrow(new EntityNotFoundException("Trainer not found"));

            // Act & Assert
            mockMvc.perform(get("/trainings/trainers/unknown.trainer")
                            .header("Password", "pass")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Trainer not found"));
        }

        @Test
        @DisplayName("Should return 400 when Password header is missing")
        void shouldReturn400WhenPasswordHeaderIsMissing() throws Exception {
            // Arrange
            GetTrainerTrainingsCriteriaRequest criteria =
                    new GetTrainerTrainingsCriteriaRequest(null, null, null);

            // Act & Assert
            mockMvc.perform(get("/trainings/trainers/jane.smith")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(criteria)))
                    .andExpect(status().isBadRequest());
        }
    }
}

