package com.epam.gymcrmspringboot.service;

import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.UpdateTraineeProfileRequest;
import com.epam.gymcrmspringboot.dto.response.GetTraineeProfileResponse;
import com.epam.gymcrmspringboot.dto.response.RegistrationResponse;
import com.epam.gymcrmspringboot.dto.response.TraineeResponse;
import com.epam.gymcrmspringboot.dto.response.TrainerSummary;
import com.epam.gymcrmspringboot.dto.response.UpdateTraineeProfileResponse;
import com.epam.gymcrmspringboot.model.TraineeEntity;

import java.util.List;

public interface TraineeService {

    RegistrationResponse registerTrainee(CreateTraineeRequest request);

    GetTraineeProfileResponse getTraineeByUsername(String username, String password);

    UpdateTraineeProfileResponse updateTrainee(String username, String password, UpdateTraineeProfileRequest request);

    List<TrainerSummary> updateTraineeTrainers(String username, String password, List<String> trainerUsernames);

    void deactivateTrainee(String username, String password);

    void activateTrainee(String username, String password);

    void deleteTrainee(String username, String password);

    boolean isRegisteredAsTrainee(String firstName, String lastName);

    TraineeEntity getTraineeByUsername(String username);

    TraineeEntity getTraineeByUsernameWithTrainings(String username);

    List<TrainerSummary> getAvailableTrainersForTrainee(String traineeUsername, String password);

}
