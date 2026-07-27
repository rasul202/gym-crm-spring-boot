package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.client.WorkloadClient;
import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.exception.TrainerWorkloadException;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import com.epam.gymcrmspringboot.service.WorkloadClientService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkloadClientServiceImpl implements WorkloadClientService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadClientServiceImpl.class);
    private static final String WORKLOAD_CIRCUIT_BREAKER = "workloadServiceCircuitBreaker";

    WorkloadClient workloadClient;
    AuthenticationService authenticationService;
    CircuitBreakerRegistry circuitBreakerRegistry;

    @PostConstruct
    void subscribeToCircuitBreakerStateTransitions() {
        io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker =
                circuitBreakerRegistry.circuitBreaker(WORKLOAD_CIRCUIT_BREAKER);
        circuitBreaker.getEventPublisher().onStateTransition(event -> {
            switch (event.getStateTransition().getToState()) {
                case OPEN -> log.warn("Workload circuit breaker state is OPEN. Workload server is not available; requests are blocked.");
                case HALF_OPEN -> log.info("Workload circuit breaker state is HALF_OPEN. Limited test requests are allowed.");
                case CLOSED -> log.info("Workload circuit breaker state is CLOSED. Normal traffic to workload server is resumed.");
                default -> log.debug("Workload circuit breaker state changed: {}", event.getStateTransition());
            }
        });
    }

    @Override
    @CircuitBreaker(name = WORKLOAD_CIRCUIT_BREAKER, fallbackMethod = "notifyWorkloadAddFallback")
    public void notifyWorkloadAdd(String trainerUsername, String trainerFirstName, String trainerLastName,
                                  boolean isActive, LocalDate trainingDate, double trainingDuration) {
        notifyWorkload(trainerUsername, trainerFirstName, trainerLastName, isActive, trainingDate, trainingDuration,
                ActionType.ADD);
    }

    @Override
    @CircuitBreaker(name = WORKLOAD_CIRCUIT_BREAKER, fallbackMethod = "notifyWorkloadDeleteFallback")
    public void notifyWorkloadDelete(String trainerUsername, String trainerFirstName, String trainerLastName,
                                     boolean isActive, LocalDate trainingDate, double trainingDuration) {
        notifyWorkload(trainerUsername, trainerFirstName, trainerLastName, isActive, trainingDate, trainingDuration,
                ActionType.DELETE);
    }

    private void notifyWorkload(String trainerUsername, String trainerFirstName, String trainerLastName,
                                boolean isActive, LocalDate trainingDate, double trainingDuration,
                                ActionType actionType) {
        String authorizationHeader;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest httpRequest = attributes.getRequest();
            String token = authenticationService.extractTokenFromAuthorizationHeader(
                    httpRequest.getHeader("Authorization"));
            authorizationHeader = token == null || token.isBlank() ? "" : "Bearer " + token;
        } else {
            authorizationHeader = "";
        }

        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                trainerUsername,
                trainerFirstName,
                trainerLastName,
                isActive,
                trainingDate,
                trainingDuration,
                actionType
        );

        workloadClient.notifyWorkload(request, authorizationHeader);
    }

    public void notifyWorkloadAddFallback(String trainerUsername, String trainerFirstName, String trainerLastName,
                                          boolean isActive, LocalDate trainingDate, double trainingDuration,
                                          Throwable throwable) {
        log.warn("Workload server is not available. Skipping ADD workload notification for trainerUsername={}.",
                trainerUsername, throwable);
        throw toTrainerWorkloadException("ADD", trainerUsername, throwable);
    }

    public void notifyWorkloadDeleteFallback(String trainerUsername, String trainerFirstName, String trainerLastName,
                                             boolean isActive, LocalDate trainingDate, double trainingDuration,
                                             Throwable throwable) {
        log.warn("Workload server is not available. Skipping DELETE workload notification for trainerUsername={}.",
                trainerUsername, throwable);
        throw toTrainerWorkloadException("DELETE", trainerUsername, throwable);
    }

    private TrainerWorkloadException toTrainerWorkloadException(String action, String trainerUsername, Throwable throwable) {
        if (throwable instanceof TrainerWorkloadException trainerWorkloadException) {
            return trainerWorkloadException;
        }
        return new TrainerWorkloadException(
                "External workload service failed while processing " + action + " notification for trainerUsername=" + trainerUsername,
                throwable
        );
    }

}
