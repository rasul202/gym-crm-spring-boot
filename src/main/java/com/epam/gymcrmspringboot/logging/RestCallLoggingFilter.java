package com.epam.gymcrmspringboot.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RestCallLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestCallLoggingFilter.class);
    private static final int MAX_BODY_LOG_LENGTH = 2000;
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        long startTimeNs = System.nanoTime();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String txId = wrappedResponse.getHeader(TransactionContext.TRANSACTION_ID_HEADER);
            if (txId != null && !txId.isBlank()) {
                TransactionContext.put(txId);
            }

            try {
                long durationMs = (System.nanoTime() - startTimeNs) / 1_000_000;
                String query = sanitizeSecrets(request.getQueryString());
                String requestBody = sanitizeSecrets(readRequestBody(wrappedRequest));
                String responseBody = sanitizeSecrets(readResponseBody(wrappedResponse));

                LOGGER.info(
                        "REST call method={} uri={} query={} status={} durationMs={} requestBody={} responseBody={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        truncate(query),
                        wrappedResponse.getStatus(),
                        durationMs,
                        truncate(requestBody),
                        truncate(responseBody));
            } finally {
                wrappedResponse.copyBodyToResponse();
                TransactionContext.clear();
            }
        }
    }

    private String readRequestBody(ContentCachingRequestWrapper request) {
        byte[] body = request.getContentAsByteArray();
        if (body.length == 0) {
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private String readResponseBody(ContentCachingResponseWrapper response) {
        byte[] body = response.getContentAsByteArray();
        if (body.length == 0) {
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    private String sanitizeSecrets(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String sanitized = value;
        sanitized = sanitized.replaceAll("(?i)(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
        sanitized = sanitized.replaceAll("(?i)(\"oldPassword\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
        sanitized = sanitized.replaceAll("(?i)(\"newPassword\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
        sanitized = sanitized.replaceAll("(?i)(password=)([^&\\s]+)", "$1***");
        sanitized = sanitized.replaceAll("(?i)(oldPassword=)([^&\\s]+)", "$1***");
        sanitized = sanitized.replaceAll("(?i)(newPassword=)([^&\\s]+)", "$1***");

        return sanitized;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_BODY_LOG_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_BODY_LOG_LENGTH) + "...(truncated)";
    }
}


