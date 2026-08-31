package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.dto.request.AddTrainingRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTraineeRequest;
import com.epam.gymcrmspringboot.dto.request.CreateTrainerRequest;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;

public class BddTestContext {

    private Integer lastResponseStatus;
    private String lastResponseBody;
    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
    private String trainerPassword;
    private String trainerToken;
    private String traineeToken;
    private Long createdTrainingId;
    private AddTrainingRequest addTrainingRequest;
    private LoginRequest loginRequest;
    private CreateTrainerRequest createTrainerRequest;
    private CreateTraineeRequest createTraineeRequest;
    private TrainerWorkloadRequest lastWorkloadMessage;

    public Integer getLastResponseStatus() {
        return lastResponseStatus;
    }

    public void setLastResponseStatus(Integer lastResponseStatus) {
        this.lastResponseStatus = lastResponseStatus;
    }

    public String getLastResponseBody() {
        return lastResponseBody;
    }

    public void setLastResponseBody(String lastResponseBody) {
        this.lastResponseBody = lastResponseBody;
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTraineePassword() {
        return traineePassword;
    }

    public void setTraineePassword(String traineePassword) {
        this.traineePassword = traineePassword;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTrainerPassword() {
        return trainerPassword;
    }

    public void setTrainerPassword(String trainerPassword) {
        this.trainerPassword = trainerPassword;
    }

    public String getTrainerToken() {
        return trainerToken;
    }

    public void setTrainerToken(String trainerToken) {
        this.trainerToken = trainerToken;
    }

    public String getTraineeToken() {
        return traineeToken;
    }

    public void setTraineeToken(String traineeToken) {
        this.traineeToken = traineeToken;
    }

    public Long getCreatedTrainingId() {
        return createdTrainingId;
    }

    public void setCreatedTrainingId(Long createdTrainingId) {
        this.createdTrainingId = createdTrainingId;
    }

    public AddTrainingRequest getAddTrainingRequest() {
        return addTrainingRequest;
    }

    public void setAddTrainingRequest(AddTrainingRequest addTrainingRequest) {
        this.addTrainingRequest = addTrainingRequest;
    }

    public LoginRequest getLoginRequest() {
        return loginRequest;
    }

    public void setLoginRequest(LoginRequest loginRequest) {
        this.loginRequest = loginRequest;
    }

    public CreateTrainerRequest getCreateTrainerRequest() {
        return createTrainerRequest;
    }

    public void setCreateTrainerRequest(CreateTrainerRequest createTrainerRequest) {
        this.createTrainerRequest = createTrainerRequest;
    }

    public CreateTraineeRequest getCreateTraineeRequest() {
        return createTraineeRequest;
    }

    public void setCreateTraineeRequest(CreateTraineeRequest createTraineeRequest) {
        this.createTraineeRequest = createTraineeRequest;
    }

    public TrainerWorkloadRequest getLastWorkloadMessage() {
        return lastWorkloadMessage;
    }

    public void setLastWorkloadMessage(TrainerWorkloadRequest lastWorkloadMessage) {
        this.lastWorkloadMessage = lastWorkloadMessage;
    }

    public void clear() {
        lastResponseStatus = null;
        lastResponseBody = null;
        traineeUsername = null;
        traineePassword = null;
        trainerUsername = null;
        trainerPassword = null;
        trainerToken = null;
        traineeToken = null;
        createdTrainingId = null;
        addTrainingRequest = null;
        loginRequest = null;
        createTrainerRequest = null;
        createTraineeRequest = null;
        lastWorkloadMessage = null;
    }
}
