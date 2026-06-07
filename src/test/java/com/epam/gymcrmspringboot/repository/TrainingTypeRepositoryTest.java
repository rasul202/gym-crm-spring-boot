package com.epam.gymcrmspringboot.repository;

import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
@ActiveProfiles("test")
@Transactional
@DisplayName("TrainingTypeRepository Tests")
class TrainingTypeRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    private TrainingTypeEntity trainingTypeEntity;

    @BeforeEach
    void setUp() {
        trainingTypeEntity = TrainingTypeEntity.builder()
                .trainingTypeName("Yoga")
                .build();
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @DisplayName("Should create and retrieve training type by ID")
        void testCreateAndRetrieveTrainingType() {
            // Arrange & Act
            TrainingTypeEntity saved = trainingTypeRepository.save(trainingTypeEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(trainingTypeRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should update training type successfully")
        void testUpdateTrainingType() {
            // Arrange
            TrainingTypeEntity saved = trainingTypeRepository.save(trainingTypeEntity);

            // Act
            saved.setTrainingTypeName("Advanced Yoga");
            TrainingTypeEntity updated = trainingTypeRepository.save(saved);

            // Assert
            assertEquals("Advanced Yoga", updated.getTrainingTypeName());
        }

        @Test
        @DisplayName("Should delete training type successfully")
        void testDeleteTrainingType() {
            // Arrange
            TrainingTypeEntity saved = trainingTypeRepository.save(trainingTypeEntity);

            // Act
            trainingTypeRepository.delete(saved);

            // Assert
            assertFalse(trainingTypeRepository.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("Should retrieve all training types")
        void testFindAllTrainingTypes() {
            // Arrange
            trainingTypeRepository.save(trainingTypeEntity);

            TrainingTypeEntity anotherType = TrainingTypeEntity.builder()
                    .trainingTypeName("Pilates")
                    .build();
            trainingTypeRepository.save(anotherType);

            // Act
            var allTypes = trainingTypeRepository.findAll();

            // Assert
            assertEquals(2, allTypes.size());
        }
    }

    @Nested
    @DisplayName("findByTrainingTypeNameIgnoreCase Tests")
    class FindByTrainingTypeNameIgnoreCaseTests {

        @Test
        @DisplayName("Should find training type by exact name")
        void testFindByExactName() {
            // Arrange
            trainingTypeRepository.save(trainingTypeEntity);

            // Act
            Optional<TrainingTypeEntity> result = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("Yoga");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Yoga", result.get().getTrainingTypeName());
        }

        @Test
        @DisplayName("Should find training type with case-insensitive search")
        void testFindWithCaseInsensitive() {
            // Arrange
            trainingTypeRepository.save(trainingTypeEntity);

            // Act
            Optional<TrainingTypeEntity> result1 = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("YOGA");
            Optional<TrainingTypeEntity> result2 = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("yoga");
            Optional<TrainingTypeEntity> result3 = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("YoGa");

            // Assert
            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertTrue(result3.isPresent());
            assertEquals("Yoga", result1.get().getTrainingTypeName());
            assertEquals("Yoga", result2.get().getTrainingTypeName());
            assertEquals("Yoga", result3.get().getTrainingTypeName());
        }

        @Test
        @DisplayName("Should return empty Optional when training type not found")
        void testFindByNameNotFound() {
            // Act
            Optional<TrainingTypeEntity> result = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("NonExistent");

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should find correct training type among multiple types")
        void testFindCorrectTypeAmongMultiple() {
            // Arrange
            trainingTypeRepository.save(trainingTypeEntity);

            TrainingTypeEntity pilates = TrainingTypeEntity.builder()
                    .trainingTypeName("Pilates")
                    .build();
            trainingTypeRepository.save(pilates);

            TrainingTypeEntity cardio = TrainingTypeEntity.builder()
                    .trainingTypeName("Cardio")
                    .build();
            trainingTypeRepository.save(cardio);

            // Act
            Optional<TrainingTypeEntity> result = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("Pilates");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Pilates", result.get().getTrainingTypeName());
        }
    }

    @Nested
    @DisplayName("Uniqueness Tests")
    class UniquenessTests {

        @Test
        @DisplayName("Should enforce unique training type name constraint")
        void testUniqueTrainingTypeNameConstraint() {
            // Arrange
            trainingTypeRepository.save(trainingTypeEntity);

            TrainingTypeEntity duplicateType = TrainingTypeEntity.builder()
                    .trainingTypeName("Yoga")
                    .build();

            // Act & Assert
            assertThrows(Exception.class, () -> trainingTypeRepository.save(duplicateType));
        }
    }

    @Nested
    @DisplayName("Field Persistence Tests")
    class FieldPersistenceTests {

        @Test
        @DisplayName("Should persist training type name correctly")
        void testPersistTrainingTypeName() {
            // Arrange & Act
            TrainingTypeEntity saved = trainingTypeRepository.save(trainingTypeEntity);

            // Assert
            assertEquals("Yoga", saved.getTrainingTypeName());
        }

        @Test
        @DisplayName("Should generate and persist ID correctly")
        void testGenerateAndPersistId() {
            // Arrange & Act
            TrainingTypeEntity saved = trainingTypeRepository.save(trainingTypeEntity);

            // Assert
            assertNotNull(saved.getId());
            assertTrue(saved.getId() > 0);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle training type names with spaces")
        void testHandleNamesWithSpaces() {
            // Arrange
            TrainingTypeEntity spacedType = TrainingTypeEntity.builder()
                    .trainingTypeName("Advanced Yoga Classes")
                    .build();
            trainingTypeRepository.save(spacedType);

            // Act
            Optional<TrainingTypeEntity> result = trainingTypeRepository
                    .findByTrainingTypeNameIgnoreCase("Advanced Yoga Classes");

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Advanced Yoga Classes", result.get().getTrainingTypeName());
        }

        @Test
        @DisplayName("Should handle training type names with mixed case persistence")
        void testPersistsMixedCaseNames() {
            // Arrange
            TrainingTypeEntity mixedCaseType = TrainingTypeEntity.builder()
                    .trainingTypeName("MyCustomTrainingType")
                    .build();

            // Act
            TrainingTypeEntity saved = trainingTypeRepository.save(mixedCaseType);

            // Assert
            assertEquals("MyCustomTrainingType", saved.getTrainingTypeName());
        }
    }
}

