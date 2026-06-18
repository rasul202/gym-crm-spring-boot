package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTrainerProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTrainerProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTrainerProfileResponse;
import com.epam.gymcrmspringboot.model.TrainerEntity;

import java.util.List;

public interface TrainerService {

    RegistrationResponse registerTrainer(CreateTrainerRequest request);

    UpdateTrainerProfileResponse updateTrainer(String username, String password, UpdateTrainerProfileRequest request);

    GetTrainerProfileResponse getTrainerByUsername(String username, String password);

    void deactivateTrainer(String username, String password);

    void activateTrainer(String username, String password);

    //Get trainers list that not assigned on trainee by trainee's username.
    List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, String password);

    List<TrainerEntity> getAllTrainersUsernamesIn(List<String> usernames);

    boolean isRegisteredAsTrainer(String firstName, String lastName);

    //for internal use cases not for API calls
    TrainerEntity getTrainerByUsername(String username);

}
