package com.epam.gymcrmspringboot.controller;

import com.epam.gymcrmspringboot.dto.response.TrainingTypeResponse;
import com.epam.gymcrmspringboot.handler.GlobalExceptionHandler;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link TrainingTypeController}.
 * Uses standaloneSetup (plain-Spring equivalent of @WebMvcTest) — no full context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingTypeController Tests")
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /training-types
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("GET /training-types")
    class GetAllTrainingTypesTests {

        @Test
        @DisplayName("Should return 200 with all training types")
        void shouldReturn200WithAllTrainingTypes() throws Exception {
            // Arrange
            List<TrainingTypeResponse> types = List.of(
                    new TrainingTypeResponse(1L, "Yoga"),
                    new TrainingTypeResponse(2L, "Pilates"),
                    new TrainingTypeResponse(3L, "Cardio"));
            when(trainingTypeService.getAllTrainingTypes()).thenReturn(types);

            // Act & Assert
            mockMvc.perform(get("/training-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].trainingTypeId").value(1))
                    .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"))
                    .andExpect(jsonPath("$[1].trainingTypeName").value("Pilates"))
                    .andExpect(jsonPath("$[2].trainingTypeName").value("Cardio"));

            verify(trainingTypeService).getAllTrainingTypes();
        }

        @Test
        @DisplayName("Should return 200 with empty list when no training types exist")
        void shouldReturn200WithEmptyListWhenNoTypesExist() throws Exception {
            // Arrange
            when(trainingTypeService.getAllTrainingTypes()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/training-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(trainingTypeService).getAllTrainingTypes();
        }

        @Test
        @DisplayName("Should return 200 with single training type")
        void shouldReturn200WithSingleTrainingType() throws Exception {
            // Arrange
            when(trainingTypeService.getAllTrainingTypes())
                    .thenReturn(List.of(new TrainingTypeResponse(5L, "Boxing")));

            // Act & Assert
            mockMvc.perform(get("/training-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].trainingTypeId").value(5))
                    .andExpect(jsonPath("$[0].trainingTypeName").value("Boxing"));
        }

        @Test
        @DisplayName("Should return 500 when service throws unexpected exception")
        void shouldReturn500WhenServiceThrowsUnexpectedException() throws Exception {
            // Arrange
            when(trainingTypeService.getAllTrainingTypes())
                    .thenThrow(new RuntimeException("Database connection lost"));

            // Act & Assert
            mockMvc.perform(get("/training-types"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }
    }
}

