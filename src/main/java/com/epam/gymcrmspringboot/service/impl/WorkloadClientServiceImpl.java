package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.exception.TrainerWorkloadException;
import com.epam.gymcrmspringboot.service.WorkloadClientService;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkloadClientServiceImpl implements WorkloadClientService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadClientServiceImpl.class);

    JmsTemplate jmsTemplate;
    RequestValidator requestValidator;
    String trainerWorkloadQueue;
    String trainerWorkloadInvalidDlq;

    public WorkloadClientServiceImpl(
            JmsTemplate jmsTemplate,
            RequestValidator requestValidator,
            @Value("${app.messaging.queue.trainer-workload}") String trainerWorkloadQueue,
            @Value("${app.messaging.queue.trainer-workload-invalid-dlq}") String trainerWorkloadInvalidDlq
    ) {
        this.jmsTemplate = jmsTemplate;
        this.requestValidator = requestValidator;
        this.trainerWorkloadQueue = trainerWorkloadQueue;
        this.trainerWorkloadInvalidDlq = trainerWorkloadInvalidDlq;
    }

    @Override
    public void notifyWorkloadAdd(String trainerUsername, String trainerFirstName, String trainerLastName,
                                  boolean isActive, LocalDate trainingDate, Integer trainingDuration) {
        notifyWorkload(trainerUsername, trainerFirstName, trainerLastName, isActive, trainingDate, trainingDuration,
                ActionType.ADD);
    }

    @Override
    public void notifyWorkloadDelete(String trainerUsername, String trainerFirstName, String trainerLastName,
                                     boolean isActive, LocalDate trainingDate, Integer trainingDuration) {
        notifyWorkload(trainerUsername, trainerFirstName, trainerLastName, isActive, trainingDate, trainingDuration,
                ActionType.DELETE);
    }

    private void notifyWorkload(String trainerUsername, String trainerFirstName, String trainerLastName,
                                boolean isActive, LocalDate trainingDate, Integer trainingDuration,
                                ActionType actionType) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                trainerUsername,
                trainerFirstName,
                trainerLastName,
                isActive,
                trainingDate,
                trainingDuration,
                actionType
        );

        List<String> invalidFields = requestValidator.validate(request);
        try {
            if (!invalidFields.isEmpty()) {
                log.warn("Routing to DLQ [{}]: missing/invalid fields: {}",
                        trainerWorkloadInvalidDlq, invalidFields);
                jmsTemplate.convertAndSend(trainerWorkloadInvalidDlq, request);
                return;
            }
            jmsTemplate.convertAndSend(trainerWorkloadQueue, request);
            log.info("Sent {} trainer workload message for trainerUsername={} to queue={}",
                    actionType, trainerUsername, trainerWorkloadQueue);
        } catch (JmsException ex) {
            log.error("Failed to send {} trainer workload message for trainerUsername={} to queue={}",
                    actionType, trainerUsername, trainerWorkloadQueue, ex);
            throw new TrainerWorkloadException(
                    "Failed to send " + actionType + " workload notification for trainerUsername=" + trainerUsername,
                    ex
            );
        }
    }

}
