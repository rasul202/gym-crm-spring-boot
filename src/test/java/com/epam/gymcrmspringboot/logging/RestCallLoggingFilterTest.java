package com.epam.gymcrmspringboot.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RestCallLoggingFilter tests")
class RestCallLoggingFilterTest {

    private final RestCallLoggingFilter filter = new RestCallLoggingFilter();

    @AfterEach
    void clearMdc() {
        TransactionContext.clear();
    }

    @Test
    @DisplayName("Logs endpoint, sanitized request details and response details")
    void shouldLogRequestAndResponseDetailsWithMasking() throws ServletException, IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(RestCallLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/password");
            request.setQueryString("username=John.Doe&password=secret123");
            request.setContentType("application/json");
            request.setCharacterEncoding("UTF-8");
            request.setContent("{\"username\":\"John.Doe\",\"newPassword\":\"newPass\"}".getBytes());
            request.addHeader(TransactionContext.TRANSACTION_ID_HEADER, "tx-123");

            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setCharacterEncoding("UTF-8");
            MockFilterChain chain = new MockFilterChain(new HttpServlet() {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
                    HttpResponseWriter.write(res, 200, "{\"message\":\"changed\",\"password\":\"dont-log\"}");
                }
            });

            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus());
            assertEquals("{\"message\":\"changed\",\"password\":\"dont-log\"}", response.getContentAsString());

            ILoggingEvent event = appender.list.stream()
                    .filter(logEvent -> logEvent.getLevel() == Level.INFO)
                    .findFirst()
                    .orElseThrow();

            String logLine = event.getFormattedMessage();
            assertTrue(logLine.contains("method=POST"));
            assertTrue(logLine.contains("uri=/users/password"));
            assertTrue(logLine.contains("status=200"));
            assertTrue(logLine.contains("password=***"));
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("Keeps response body available after logging")
    void shouldCopyResponseBodyBackToClient() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/users/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
                HttpResponseWriter.write(res, 401, "{\"message\":\"invalid credentials\"}");
            }
        });

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertEquals("{\"message\":\"invalid credentials\"}", response.getContentAsString());
    }

    private static final class HttpResponseWriter {
        private HttpResponseWriter() {
        }

        private static void write(HttpServletResponse response, int status, String body) throws IOException {
            response.setStatus(status);
            response.setContentType("application/json");
            response.getWriter().write(body);
            response.getWriter().flush();
        }
    }
}



