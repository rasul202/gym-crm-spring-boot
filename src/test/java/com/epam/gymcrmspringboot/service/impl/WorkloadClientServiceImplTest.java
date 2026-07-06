package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.client.WorkloadClient;
import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.service.AuthenticationService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import io.github.resilience4j.core.EventConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadClientServiceImpl Tests")
class WorkloadClientServiceImplTest {

    @Mock
    private WorkloadClient workloadClient;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private io.github.resilience4j.circuitbreaker.CircuitBreaker circuitBreaker;

    @Mock
    private io.github.resilience4j.circuitbreaker.CircuitBreaker.EventPublisher eventPublisher;

    private WorkloadClientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkloadClientServiceImpl(workloadClient, authenticationService, circuitBreakerRegistry);
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("Should notify workload add with bearer token when request header is present")
    void shouldNotifyWorkloadAddWithBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(authenticationService.extractTokenFromAuthorizationHeader("Bearer valid-token")).thenReturn("valid-token");

        service.notifyWorkloadAdd("trainer.user", "John", "Smith", true, LocalDate.of(2026, 7, 6), 60.0);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(workloadClient).notifyWorkload(requestCaptor.capture(), headerCaptor.capture());

        TrainerWorkloadRequest actual = requestCaptor.getValue();
        assertAll(
                () -> assertEquals("trainer.user", actual.getTrainerUsername()),
                () -> assertEquals("John", actual.getTrainerFirstName()),
                () -> assertEquals("Smith", actual.getTrainerLastName()),
                () -> assertTrue(actual.getIsActive()),
                () -> assertEquals(LocalDate.of(2026, 7, 6), actual.getTrainingDate()),
                () -> assertEquals(60.0, actual.getTrainingDuration()),
                () -> assertEquals(ActionType.ADD, actual.getActionType()),
                () -> assertEquals("Bearer valid-token", headerCaptor.getValue())
        );

        verify(authenticationService).extractTokenFromAuthorizationHeader("Bearer valid-token");
    }

    @Test
    @DisplayName("Should notify workload add with empty authorization when token extraction returns blank")
    void shouldNotifyWorkloadAddWithEmptyAuthorizationWhenTokenBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer   ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(authenticationService.extractTokenFromAuthorizationHeader("Bearer   ")).thenReturn(" ");

        service.notifyWorkloadAdd("trainer.user", "John", "Smith", false, LocalDate.of(2026, 7, 6), 45.5);

        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(workloadClient).notifyWorkload(any(TrainerWorkloadRequest.class), headerCaptor.capture());
        assertEquals("", headerCaptor.getValue());
    }

    @Test
    @DisplayName("Should notify workload delete with empty authorization when request attributes are missing")
    void shouldNotifyWorkloadDeleteWithEmptyAuthorizationWhenRequestAttributesMissing() {
        RequestContextHolder.resetRequestAttributes();

        service.notifyWorkloadDelete("trainer.user", "John", "Smith", true, LocalDate.of(2026, 7, 6), 30.0);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        verify(workloadClient).notifyWorkload(requestCaptor.capture(), headerCaptor.capture());

        TrainerWorkloadRequest actual = requestCaptor.getValue();
        assertAll(
                () -> assertEquals(ActionType.DELETE, actual.getActionType()),
                () -> assertEquals("", headerCaptor.getValue())
        );
        verifyNoInteractions(authenticationService);
    }

    @Test
    @DisplayName("Should register circuit-breaker state transition handler")
    void shouldRegisterCircuitBreakerStateTransitionHandler() {
        when(circuitBreakerRegistry.circuitBreaker("workloadServiceCircuitBreaker")).thenReturn(circuitBreaker);
        when(circuitBreaker.getEventPublisher()).thenReturn(eventPublisher);
        when(eventPublisher.onStateTransition(any())).thenReturn(eventPublisher);

        service.subscribeToCircuitBreakerStateTransitions();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<EventConsumer<CircuitBreakerOnStateTransitionEvent>> captor =
                ArgumentCaptor.forClass(EventConsumer.class);

        verify(circuitBreakerRegistry).circuitBreaker("workloadServiceCircuitBreaker");
        verify(eventPublisher).onStateTransition(captor.capture());

        EventConsumer<CircuitBreakerOnStateTransitionEvent> consumer = captor.getValue();
        assertDoesNotThrow(() -> consumer.consumeEvent(
                new CircuitBreakerOnStateTransitionEvent(
                        "workloadServiceCircuitBreaker",
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.CLOSED_TO_OPEN
                )));
        assertDoesNotThrow(() -> consumer.consumeEvent(
                new CircuitBreakerOnStateTransitionEvent(
                        "workloadServiceCircuitBreaker",
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.OPEN_TO_HALF_OPEN
                )));
        assertDoesNotThrow(() -> consumer.consumeEvent(
                new CircuitBreakerOnStateTransitionEvent(
                        "workloadServiceCircuitBreaker",
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.OPEN_TO_CLOSED
                )));
        assertDoesNotThrow(() -> consumer.consumeEvent(
                new CircuitBreakerOnStateTransitionEvent(
                        "workloadServiceCircuitBreaker",
                        io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.OPEN_TO_DISABLED
                )));
    }


}
