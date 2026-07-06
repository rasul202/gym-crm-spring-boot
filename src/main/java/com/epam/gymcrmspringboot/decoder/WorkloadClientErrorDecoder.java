package com.epam.gymcrmspringboot.decoder;

import com.epam.gymcrmspringboot.exception.ExternalServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WorkloadClientErrorDecoder implements ErrorDecoder {

    static final String WORKLOAD_SERVICE_NAME = "workload-service";
    ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        ErrorBody errorBody = extractErrorBody(response);
        HttpStatus status = HttpStatus.resolve(response.status());
        String statusText = errorBody.error() != null && !errorBody.error().isBlank()
                ? errorBody.error()
                : (status != null ? status.getReasonPhrase() : "Unknown Error");
        String remoteMessage = errorBody.message() != null && !errorBody.message().isBlank()
                ? errorBody.message()
                : "No error message returned by external service";

        String message = "External workload service error [" + response.status() + " " + statusText + "] from "
                + methodKey + ": " + remoteMessage;

        return new ExternalServiceException(response.status(), WORKLOAD_SERVICE_NAME, message);
    }

    private ErrorBody extractErrorBody(Response response) {
        if (response.body() == null) {
            return new ErrorBody(null, null);
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            JsonNode node = objectMapper.readTree(inputStream);
            return new ErrorBody(readText(node, "error"), readText(node, "message"));
        } catch (IOException ex) {
            return new ErrorBody(
                    null,
                    "Failed to parse error payload from external workload service: " + ex.getMessage()
            );
        }
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText();
    }

    private record ErrorBody(String error, String message) {
    }
}
