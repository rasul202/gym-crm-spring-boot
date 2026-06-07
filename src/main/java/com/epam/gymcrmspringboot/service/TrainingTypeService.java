package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.response.TrainingTypeResponse;
import com.epam.gymcrmspringboot.model.TrainingTypeEntity;

import java.util.List;

public interface TrainingTypeService {

    TrainingTypeEntity getTrainingTypeByName(String trainingTypeName);

    List<TrainingTypeResponse> getAllTrainingTypes();
}