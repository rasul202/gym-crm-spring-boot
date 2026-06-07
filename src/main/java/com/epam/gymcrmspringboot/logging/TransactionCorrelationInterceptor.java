package com.epam.gymcrmspringboot.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TransactionCorrelationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCorrelationInterceptor.class);
    private static final String START_TIME_ATTRIBUTE = "transactionStartTimeNs";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String transactionId = TransactionContext.resolveOrGenerate(
                request.getHeader(TransactionContext.TRANSACTION_ID_HEADER));

        TransactionContext.put(transactionId);
        response.setHeader(TransactionContext.TRANSACTION_ID_HEADER, transactionId);
        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());

//        LOGGER.info("Request started method={} uri={}", request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
//            Long startedAtNs = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
//            long durationMs = startedAtNs == null ? -1 : (System.nanoTime() - startedAtNs) / 1_000_000;

//            LOGGER.info(
//                    "Request completed method={} uri={} status={} durationMs={}",
//                    request.getMethod(),
//                    request.getRequestURI(),
//                    response.getStatus(),
//                    durationMs);
        } finally {
            TransactionContext.clear();
        }
    }
}

