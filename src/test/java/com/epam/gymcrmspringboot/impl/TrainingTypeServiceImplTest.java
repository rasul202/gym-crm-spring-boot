package com.epam.gymcrmspringboot.impl;

import com.epam.gymcrmspringboot.dto.response.TrainingTypeResponse;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.repository.TrainingTypeRepository;
import com.epam.gymcrmspringboot.service.impl.TrainingTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("TrainingTypeServiceImpl Tests")
@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    private TrainingTypeEntity trainingTypeEntity;

    @BeforeEach
    void setUp() {
        trainingTypeEntity = TrainingTypeEntity.builder()
                .id(1L)
                .trainingTypeName("Yoga")
                .build();
    }

    @Test
    @DisplayName("Should get training type by name successfully")
    void testGetTrainingTypeByNameSuccess() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("Yoga"))
                .thenReturn(Optional.of(trainingTypeEntity));

        // Act
        TrainingTypeEntity result = trainingTypeService.getTrainingTypeByName("Yoga");

        // Assert
        assertNotNull(result);
        assertEquals("Yoga", result.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should find training type with case-insensitive search")
    void testGetTrainingTypeByNameCaseInsensitive() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("YOGA"))
                .thenReturn(Optional.of(trainingTypeEntity));

        // Act
        TrainingTypeEntity result = trainingTypeService.getTrainingTypeByName("YOGA");

        // Assert
        assertNotNull(result);
        assertEquals("Yoga", result.getTrainingTypeName());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when training type not found")
    void testGetTrainingTypeByNameThrowsException() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("NonExistent"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> trainingTypeService.getTrainingTypeByName("NonExistent"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when training type name is null")
    void testGetTrainingTypeByNameThrowsExceptionForNullName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.getTrainingTypeByName(null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when training type name is blank")
    void testGetTrainingTypeByNameThrowsExceptionForBlankName() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.getTrainingTypeByName("   "));
        assertThrows(IllegalArgumentException.class,
                () -> trainingTypeService.getTrainingTypeByName(""));
    }

    @Test
    @DisplayName("Should trim training type name before searching")
    void testGetTrainingTypeByNameTrimsName() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeNameIgnoreCase("Yoga"))
                .thenReturn(Optional.of(trainingTypeEntity));

        // Act
        TrainingTypeEntity result = trainingTypeService.getTrainingTypeByName("  Yoga  ");

        // Assert
        assertNotNull(result);
        verify(trainingTypeRepository).findByTrainingTypeNameIgnoreCase("Yoga");
    }

    @Test
    @DisplayName("Should return all training types mapped to response DTOs")
    void testGetAllTrainingTypesSuccess() {
        // Arrange
        TrainingTypeEntity pilatesEntity = TrainingTypeEntity.builder()
                .id(2L)
                .trainingTypeName("Pilates")
                .build();

        when(trainingTypeRepository.findAll())
                .thenReturn(List.of(trainingTypeEntity, pilatesEntity));

        // Act
        List<TrainingTypeResponse> result = trainingTypeService.getAllTrainingTypes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Yoga", result.get(0).getTrainingTypeName());
        assertEquals("Pilates", result.get(1).getTrainingTypeName());
    }

    @Test
    @DisplayName("Should return empty list when no training types exist")
    void testGetAllTrainingTypesReturnsEmptyList() {
        // Arrange
        when(trainingTypeRepository.findAll()).thenReturn(List.of());

        // Act
        List<TrainingTypeResponse> result = trainingTypeService.getAllTrainingTypes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

