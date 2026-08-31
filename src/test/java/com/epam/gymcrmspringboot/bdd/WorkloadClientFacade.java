package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.service.WorkloadClientService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkloadClientFacade {

    private final WorkloadClientService workloadClientService;

    public WorkloadClientFacade(WorkloadClientService workloadClientService) {
        this.workloadClientService = workloadClientService;
    }

    public void notifyWorkloadAdd(
            String trainerUsername,
            String trainerFirstName,
            String trainerLastName,
            boolean isActive,
            LocalDate trainingDate,
            Integer trainingDuration,
            Long trainingId) {
        workloadClientService.notifyWorkloadAdd(
                trainerUsername,
                trainerFirstName,
                trainerLastName,
                isActive,
                trainingDate,
                trainingDuration,
                trainingId);
    }
}
