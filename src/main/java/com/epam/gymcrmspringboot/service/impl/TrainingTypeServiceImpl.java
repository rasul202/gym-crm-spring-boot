package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.response.TrainingTypeResponse;
import com.epam.gymcrmspringboot.exception.EntityNotFoundException;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.repository.TrainingTypeRepository;
import com.epam.gymcrmspringboot.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingTypeServiceImpl.class);

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Override
    public TrainingTypeEntity getTrainingTypeByName(String trainingTypeName) {
        LOGGER.info("Get training type by name operation has been started for trainingTypeName={}", trainingTypeName);
        if (trainingTypeName == null || trainingTypeName.isBlank()) {
            throw new IllegalArgumentException("trainingTypeName must not be blank");
        }

        return trainingTypeRepository.findByTrainingTypeNameIgnoreCase(trainingTypeName.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No training type found with name: " + trainingTypeName.trim()));
    }

    @Override
    public List<TrainingTypeResponse> getAllTrainingTypes() {
        LOGGER.info("Get all training types operation has been started");
        return trainingTypeRepository.findAll().stream()
                .map(entity -> new TrainingTypeResponse(entity.getId(), entity.getTrainingTypeName()))
                .collect(Collectors.toList());
    }
}
