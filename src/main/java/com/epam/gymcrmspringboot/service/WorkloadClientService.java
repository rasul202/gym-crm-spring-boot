package com.epam.gymcrmspringboot.service;

import java.time.LocalDate;

public interface WorkloadClientService {

    void notifyWorkloadDelete(String trainerUsername, String trainerFirstName, String trainerLastName,
                              boolean isActive, LocalDate trainingDate, double trainingDuration);

    void notifyWorkloadAdd(String trainerUsername, String trainerFirstName, String trainerLastName,
                           boolean isActive, LocalDate trainingDate, double trainingDuration);

}
