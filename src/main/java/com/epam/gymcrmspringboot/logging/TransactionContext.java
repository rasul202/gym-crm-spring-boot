package com.epam.gymcrmspringboot.logging;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public final class TransactionContext {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    public static String resolveOrGenerate(String incomingTransactionId) {
        if (incomingTransactionId == null || incomingTransactionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incomingTransactionId.trim();
    }

    public static void put(String transactionId) {
        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
    }

    public static Optional<String> getCurrentTransactionId() {
        return Optional.ofNullable(MDC.get(TRANSACTION_ID_MDC_KEY));
    }

    public static void clear() {
        MDC.remove(TRANSACTION_ID_MDC_KEY);
    }
}

