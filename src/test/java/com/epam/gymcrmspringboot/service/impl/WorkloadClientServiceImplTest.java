package com.epam.gymcrmspringboot.service.impl;

import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.exception.TrainerWorkloadException;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkloadClientServiceImpl Tests")
class WorkloadClientServiceImplTest {

    private static final String WORKLOAD_QUEUE = "trainer.workload.queue";
    private static final String INVALID_DLQ = "trainer.workload.invalid.dlq";

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private RequestValidator requestValidator;

    private WorkloadClientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WorkloadClientServiceImpl(jmsTemplate, requestValidator, WORKLOAD_QUEUE, INVALID_DLQ);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(jmsTemplate);
    }

    @Test
    @DisplayName("Should send ADD trainer workload message to configured queue")
    void shouldSendAddMessageToQueue() {
        when(requestValidator.validate(any(TrainerWorkloadRequest.class))).thenReturn(List.of());

        service.notifyWorkloadAdd("trainer.user", "John", "Smith", true, LocalDate.of(2026, 7, 6), 60, 1L);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq(WORKLOAD_QUEUE), requestCaptor.capture());

        TrainerWorkloadRequest actual = requestCaptor.getValue();
        assertAll(
                () -> assertEquals("trainer.user", actual.getTrainerUsername()),
                () -> assertEquals("John", actual.getTrainerFirstName()),
                () -> assertEquals("Smith", actual.getTrainerLastName()),
                () -> assertTrue(actual.getIsActive()),
                () -> assertEquals(LocalDate.of(2026, 7, 6), actual.getTrainingDate()),
                () -> assertEquals(60, actual.getTrainingDuration()),
                () -> assertEquals(1L, actual.getTrainingId()),
                () -> assertEquals(ActionType.ADD, actual.getActionType())
        );
    }

    @Test
    @DisplayName("Should send DELETE trainer workload message to configured queue")
    void shouldSendDeleteMessageToQueue() {
        when(requestValidator.validate(any(TrainerWorkloadRequest.class))).thenReturn(List.of());

        service.notifyWorkloadDelete("trainer.user", "John", "Smith", true, LocalDate.of(2026, 7, 6), 30);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor = ArgumentCaptor.forClass(TrainerWorkloadRequest.class);
        verify(jmsTemplate).convertAndSend(eq(WORKLOAD_QUEUE), requestCaptor.capture());

        TrainerWorkloadRequest actual = requestCaptor.getValue();
        assertEquals(ActionType.DELETE, actual.getActionType());
    }

    @Test
    @DisplayName("Should wrap JMS send exception in TrainerWorkloadException")
    void shouldWrapJmsException() {
        when(requestValidator.validate(any(TrainerWorkloadRequest.class))).thenReturn(List.of());

        doThrow(new JmsException("Broker unavailable") {
        }).when(jmsTemplate).convertAndSend(eq(WORKLOAD_QUEUE), any(TrainerWorkloadRequest.class));

        TrainerWorkloadException ex = assertThrows(TrainerWorkloadException.class,
                () -> service.notifyWorkloadAdd("trainer.user", "John", "Smith", true, LocalDate.of(2026, 7, 6), 60 ,1L));

        assertTrue(ex.getMessage().contains("Failed to send ADD workload notification"));
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof JmsException);
    }

    @Test
    @DisplayName("Should route invalid workload request to DLQ and skip main queue")
    void shouldRouteInvalidRequestToDlq() {
        when(requestValidator.validate(any(TrainerWorkloadRequest.class)))
                .thenReturn(List.of("trainerFirstName", "trainingDate"));

        service.notifyWorkloadAdd("trainer.user", " ", "Smith", true, null, 60, 1L);

        verify(jmsTemplate).convertAndSend(eq(INVALID_DLQ), any(TrainerWorkloadRequest.class));
        verify(jmsTemplate, never()).convertAndSend(eq(WORKLOAD_QUEUE), any(TrainerWorkloadRequest.class));
    }
}
