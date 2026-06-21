package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.request.GetTraineeTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.request.GetTrainerTrainingsCriteriaRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeTrainingsResponse;
import com.epam.gymcrmspringboot.dto.response.GetTrainerTrainingsResponse;
import com.epam.gymcrmspringboot.model.TraineeEntity;
import com.epam.gymcrmspringboot.model.TrainingEntity;

import org.springframework.security.core.Authentication;
import java.util.List;

public interface TrainingService {

    void addTraining(AddTrainingRequest request, Authentication authentication);

    List<GetTraineeTrainingsResponse> getTraineeTrainings(String traineeUsername , Authentication authentication, GetTraineeTrainingsCriteriaRequest request);

    List<GetTrainerTrainingsResponse> getTrainerTrainings(String trainerUsername , Authentication authentication, GetTrainerTrainingsCriteriaRequest criteria);

    //for internal use purpose not for API calls
    void deleteAllByTrainee(TraineeEntity trainee);

    //for internal use purpose not for API calls
    void saveAll(List<TrainingEntity> newTrainerAssignments);

}
