package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;

import java.time.LocalDate;
import java.util.Map;

public final class WorkloadMessageMapper {

    private WorkloadMessageMapper() {
    }

    public static TrainerWorkloadRequest fromMap(Map<?, ?> payload) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername((String) payload.get("trainerUsername"));
        request.setTrainerFirstName((String) payload.get("trainerFirstName"));
        request.setTrainerLastName((String) payload.get("trainerLastName"));
        request.setIsActive((Boolean) payload.get("isActive"));
        request.setTrainingDate(payload.get("trainingDate") == null ? null : LocalDate.parse((String) payload.get("trainingDate")));
        request.setTrainingDuration(payload.get("trainingDuration") == null ? null : ((Number) payload.get("trainingDuration")).intValue());
        request.setTrainingId(payload.get("trainingId") == null ? null : ((Number) payload.get("trainingId")).longValue());
        request.setActionType(payload.get("actionType") == null ? null : ActionType.valueOf((String) payload.get("actionType")));
        return request;
    }
}
