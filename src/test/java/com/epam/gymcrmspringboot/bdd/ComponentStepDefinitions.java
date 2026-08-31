package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.LoginRequest;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.validation.RequestValidator;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ComponentStepDefinitions {

    private final ApiClientSupport apiClientSupport;
    private final BddTestContext context;
    private final WorkloadClientFacade workloadClientFacade;
    private final RequestValidator requestValidator;
    private final ConnectionFactory connectionFactory;
    private final String workloadQueueName;
    private final String invalidDlqName;
    private final ObjectMapper objectMapper;

    public ComponentStepDefinitions(
            ApiClientSupport apiClientSupport,
            BddTestContext context,
            WorkloadClientFacade workloadClientFacade,
            RequestValidator requestValidator,
            ConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${app.messaging.queue.trainer-workload}") String workloadQueueName,
            @Value("${app.messaging.queue.trainer-workload-invalid-dlq}") String invalidDlqName) {
        this.apiClientSupport = apiClientSupport;
        this.context = context;
        this.workloadClientFacade = workloadClientFacade;
        this.requestValidator = requestValidator;
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.workloadQueueName = workloadQueueName;
        this.invalidDlqName = invalidDlqName;
    }

    @Given("a registered trainer account exists for component tests")
    public void aRegisteredTrainerAccountExistsForComponentTests() throws Exception {
        var response = apiClientSupport.post("/trainers", Map.of(
                "firstName", "Component",
                "lastName", "Trainer",
                "specialization", "Yoga"
        ));
        Assertions.assertEquals(201, response.getStatusCode().value(), response.getBody());
        Map<?, ?> body = objectMapper.readValue(response.getBody(), Map.class);
        context.setTrainerUsername((String) body.get("username"));
        context.setTrainerPassword((String) body.get("password"));
    }

    @Given("the trainer uses the correct password for login")
    public void theTrainerUsesTheCorrectPasswordForLogin() {
        context.setLoginRequest(new LoginRequest(context.getTrainerUsername(), context.getTrainerPassword()));
    }

    @Given("the trainer uses an incorrect password for login")
    public void theTrainerUsesAnIncorrectPasswordForLogin() {
        context.setLoginRequest(new LoginRequest(context.getTrainerUsername(), "wrong-password"));
    }

    @When("the client submits the login request")
    public void theClientSubmitsTheLoginRequest() {
        var response = apiClientSupport.post("/authentication/login", Map.of(
                "username", context.getLoginRequest().getUsername(),
                "password", context.getLoginRequest().getPassword()
        ));
        context.setLastResponseStatus(response.getStatusCode().value());
        String body = response.getBody();
        if (body == null) {
            body = String.valueOf(response.getStatusCode().value());
        }
        context.setLastResponseBody(body);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(Integer expectedStatus) {
        Assertions.assertEquals(expectedStatus, context.getLastResponseStatus());
    }

    @Then("the login response should contain a JWT token")
    public void theLoginResponseShouldContainAJwtToken() {
        String body = context.getLastResponseBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.contains("token"));
        Assertions.assertFalse(body.contains("\"token\":null"));
    }

    @Then("the error response should mention invalid credentials")
    public void theErrorResponseShouldMentionInvalidCredentials() {
        String body = context.getLastResponseBody();
        Assertions.assertNotNull(body);
        String normalizedBody = body.toLowerCase();
        boolean mentionsInvalidCredentials = normalizedBody.contains("invalid credentials")
                || normalizedBody.contains("bad credentials");

        if (!mentionsInvalidCredentials) {
            try {
                Map<?, ?> errorBody = objectMapper.readValue(body, Map.class);
                Object message = errorBody.get("message");
                if (message instanceof String messageText) {
                    String normalizedMessage = messageText.toLowerCase();
                    mentionsInvalidCredentials = normalizedMessage.contains("invalid credentials")
                            || normalizedMessage.contains("bad credentials")
                            || (normalizedMessage.contains("credential") && normalizedMessage.contains("invalid"));
                }
            } catch (Exception ignored) {
                // keep the original raw-body assertion result if the response is not JSON
            }
        }

        Assertions.assertTrue(mentionsInvalidCredentials, () -> "Unexpected error response body: " + body);
    }

    @When("a valid workload add event is published")
    public void aValidWorkloadAddEventIsPublished() {
        workloadClientFacade.notifyWorkloadAdd(
                "component.trainer",
                "Component",
                "Trainer",
                true,
                LocalDate.of(2026, 8, 1),
                60,
                1001L);
    }

    @When("an invalid workload add event is published")
    public void anInvalidWorkloadAddEventIsPublished() {
        workloadClientFacade.notifyWorkloadAdd(
                "component.trainer",
                " ",
                "Trainer",
                true,
                null,
                60,
                1002L);
    }

    @Then("the main workload queue should contain an {word} event")
    public void theMainWorkloadQueueShouldContainAnEvent(String actionType) {
        TrainerWorkloadRequest message = receiveWorkloadMessage(workloadQueueName);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(ActionType.valueOf(actionType), message.getActionType());
        context.setLastWorkloadMessage(message);
    }

    @Then("the invalid workload queue should contain the workload event")
    public void theInvalidWorkloadQueueShouldContainTheWorkloadEvent() {
        TrainerWorkloadRequest message = receiveWorkloadMessage(invalidDlqName);
        Assertions.assertNotNull(message);
        context.setLastWorkloadMessage(message);
    }

    @Then("the main workload queue should stay empty")
    public void theMainWorkloadQueueShouldStayEmpty() {
        Assertions.assertNull(receiveWorkloadMessage(workloadQueueName));
    }

    @Then("the workload event should include trainer username {string}")
    public void theWorkloadEventShouldIncludeTrainerUsername(String expectedUsername) {
        Assertions.assertNotNull(context.getLastWorkloadMessage());
        Assertions.assertEquals(expectedUsername, context.getLastWorkloadMessage().getTrainerUsername());
    }

    @Then("the workload request should be considered invalid by the validator")
    public void theWorkloadRequestShouldBeConsideredInvalidByTheValidator() {
        Assertions.assertNotNull(context.getLastWorkloadMessage());
        List<String> invalidFields = requestValidator.validate(context.getLastWorkloadMessage());
        Assertions.assertFalse(invalidFields.isEmpty());
        Assertions.assertTrue(invalidFields.contains("trainerFirstName") || invalidFields.contains("trainingDate"));
    }

    private TrainerWorkloadRequest receiveWorkloadMessage(String queueName) {
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            Queue queue = jmsContext.createQueue(queueName);
            Message message = jmsContext.createConsumer(queue).receive(2000);
            if (message == null) {
                return null;
            }
            String json = message.getBody(String.class);
            Map<?, ?> payload = objectMapper.readValue(json, Map.class);
            return WorkloadMessageMapper.fromMap(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read message from queue " + queueName, ex);
        }
    }
}
