package com.epam.gymcrmspringboot.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionCorrelationInterceptor tests")
class TransactionCorrelationInterceptorTest {

    private final TransactionCorrelationInterceptor interceptor = new TransactionCorrelationInterceptor();

    @AfterEach
    void cleanUpMdc() {
        TransactionContext.clear();
    }

    @Test
    @DisplayName("Generates transaction id and returns it in response when request header is missing")
    void shouldGenerateTransactionIdWhenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/trainees/john.doe");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request, response, new Object());

        assertTrue(proceed);
        String responseTxId = response.getHeader(TransactionContext.TRANSACTION_ID_HEADER);
        assertNotNull(responseTxId);
        assertFalse(responseTxId.isBlank());
        assertEquals(responseTxId, TransactionContext.getCurrentTransactionId().orElse(null));

        interceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    @DisplayName("Reuses incoming transaction id and echoes it in response")
    void shouldReuseIncomingTransactionId() {
        String incomingTransactionId = "external-tx-123";
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/trainings");
        request.addHeader(TransactionContext.TRANSACTION_ID_HEADER, incomingTransactionId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        assertEquals(incomingTransactionId, response.getHeader(TransactionContext.TRANSACTION_ID_HEADER));
        assertEquals(incomingTransactionId, TransactionContext.getCurrentTransactionId().orElse(null));

        interceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    @DisplayName("Clears MDC after request completion to avoid leakage")
    void shouldClearMdcAfterCompletion() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/trainers/jane.doe");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());
        assertTrue(TransactionContext.getCurrentTransactionId().isPresent());

        interceptor.afterCompletion(request, response, new Object(), null);
        assertTrue(TransactionContext.getCurrentTransactionId().isEmpty());
    }

    @Test
    @DisplayName("Does not leak transaction id between requests")
    void shouldNotLeakTransactionIdBetweenRequests() {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/users/login");
        firstRequest.addHeader(TransactionContext.TRANSACTION_ID_HEADER, "tx-first");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        interceptor.preHandle(firstRequest, firstResponse, new Object());
        interceptor.afterCompletion(firstRequest, firstResponse, new Object(), null);

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/users/login");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();

        interceptor.preHandle(secondRequest, secondResponse, new Object());
        String secondTxId = secondResponse.getHeader(TransactionContext.TRANSACTION_ID_HEADER);

        assertNotNull(secondTxId);
        assertNotEquals("tx-first", secondTxId);

        interceptor.afterCompletion(secondRequest, secondResponse, new Object(), null);
        assertTrue(TransactionContext.getCurrentTransactionId().isEmpty());
    }
}

