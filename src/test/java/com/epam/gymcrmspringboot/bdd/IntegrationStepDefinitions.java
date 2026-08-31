package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.dto.ActionType;
import com.epam.gymcrmspringboot.dto.request.TrainerWorkloadRequest;
import com.epam.gymcrmspringboot.model.TrainingEntity;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import com.epam.gymcrmspringboot.repository.UserRepository;
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

public class IntegrationStepDefinitions {

    private final ApiClientSupport apiClientSupport;
    private final BddTestContext context;
    private final TrainingRepository trainingRepository;
    private final UserRepository userRepository;
    private final ConnectionFactory connectionFactory;
    private final String workloadQueueName;
    private final ObjectMapper objectMapper;

    public IntegrationStepDefinitions(
            ApiClientSupport apiClientSupport,
            BddTestContext context,
            TrainingRepository trainingRepository,
            UserRepository userRepository,
            ConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${app.messaging.queue.trainer-workload}") String workloadQueueName) {
        this.apiClientSupport = apiClientSupport;
        this.context = context;
        this.trainingRepository = trainingRepository;
        this.userRepository = userRepository;
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.workloadQueueName = workloadQueueName;
    }

    @Given("a trainer and trainee are registered for integration tests")
    public void aTrainerAndTraineeAreRegisteredForIntegrationTests() throws Exception {
        var trainerResponse = apiClientSupport.post("/trainers", Map.of(
                "firstName", "Integration",
                "lastName", "Trainer",
                "specialization", "Yoga"
        ));
        var traineeResponse = apiClientSupport.post("/trainees", Map.of(
                "firstName", "Integration",
                "lastName", "Trainee",
                "dateOfBirth", "1998-05-12",
                "address", "Main Street"
        ));

        Assertions.assertEquals(201, trainerResponse.getStatusCode().value(), trainerResponse.getBody());
        Assertions.assertEquals(201, traineeResponse.getStatusCode().value(), traineeResponse.getBody());

        Map<?, ?> trainer = objectMapper.readValue(trainerResponse.getBody(), Map.class);
        Map<?, ?> trainee = objectMapper.readValue(traineeResponse.getBody(), Map.class);

        context.setTrainerUsername((String) trainer.get("username"));
        context.setTrainerPassword((String) trainer.get("password"));
        context.setTraineeUsername((String) trainee.get("username"));
        context.setTraineePassword((String) trainee.get("password"));
    }

    @Given("the trainer is authenticated for integration tests")
    public void theTrainerIsAuthenticatedForIntegrationTests() throws Exception {
        var response = apiClientSupport.post("/authentication/login", Map.of(
                "username", context.getTrainerUsername(),
                "password", context.getTrainerPassword()
        ));
        Assertions.assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = objectMapper.readValue(response.getBody(), Map.class);
        context.setTrainerToken((String) body.get("token"));
    }

    @Given("the trainee is authenticated for integration tests")
    public void theTraineeIsAuthenticatedForIntegrationTests() throws Exception {
        var response = apiClientSupport.post("/authentication/login", Map.of(
                "username", context.getTraineeUsername(),
                "password", context.getTraineePassword()
        ));
        Assertions.assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = objectMapper.readValue(response.getBody(), Map.class);
        context.setTraineeToken((String) body.get("token"));
    }

    @Given("a valid add training request is prepared")
    public void aValidAddTrainingRequestIsPrepared() {
        context.setAddTrainingRequest(new com.epam.gymcrmspringboot.dto.request.AddTrainingRequest(
                context.getTraineeUsername(),
                context.getTrainerUsername(),
                "BDD Strength Session",
                LocalDate.of(2026, 8, 15),
                90
        ));
    }

    @Given("an invalid add training request with blank training name is prepared")
    public void anInvalidAddTrainingRequestWithBlankTrainingNameIsPrepared() {
        context.setAddTrainingRequest(new com.epam.gymcrmspringboot.dto.request.AddTrainingRequest(
                context.getTraineeUsername(),
                context.getTrainerUsername(),
                " ",
                LocalDate.of(2026, 8, 15),
                90
        ));
    }

    @When("the trainer creates the training through the API")
    public void theTrainerCreatesTheTrainingThroughTheApi() {
        Map<String, Object> payload = Map.of(
                "traineeUsername", context.getAddTrainingRequest().getTraineeUsername(),
                "trainerUsername", context.getAddTrainingRequest().getTrainerUsername(),
                "trainingName", context.getAddTrainingRequest().getTrainingName(),
                "trainingDate", context.getAddTrainingRequest().getTrainingDate().toString(),
                "trainingDuration", context.getAddTrainingRequest().getTrainingDuration()
        );
        var response = apiClientSupport.postWithBearer("/trainings", payload, context.getTrainerToken());
        context.setLastResponseStatus(response.getStatusCode().value());
        context.setLastResponseBody(response.getBody());
    }

    @When("the trainee tries to create the training through the API")
    public void theTraineeTriesToCreateTheTrainingThroughTheApi() {
        Map<String, Object> payload = Map.of(
                "traineeUsername", context.getAddTrainingRequest().getTraineeUsername(),
                "trainerUsername", context.getAddTrainingRequest().getTrainerUsername(),
                "trainingName", context.getAddTrainingRequest().getTrainingName(),
                "trainingDate", context.getAddTrainingRequest().getTrainingDate().toString(),
                "trainingDuration", context.getAddTrainingRequest().getTrainingDuration()
        );
        var response = apiClientSupport.postWithBearer("/trainings", payload, context.getTraineeToken());
        context.setLastResponseStatus(response.getStatusCode().value());
        context.setLastResponseBody(response.getBody());
    }

    @Then("the training should be persisted")
    public void theTrainingShouldBePersisted() {
        List<TrainingEntity> trainings = trainingRepository.findAll();
        Assertions.assertEquals(1, trainings.size());
        TrainingEntity training = trainings.get(0);
        context.setCreatedTrainingId(training.getId());
        Assertions.assertEquals("BDD Strength Session", training.getTrainingName());
        Assertions.assertEquals(context.getTrainerUsername(), training.getTrainer().getUser().getUsername());
        Assertions.assertEquals(context.getTraineeUsername(), training.getTrainee().getUser().getUsername());
    }

    @Then("an {word} workload event should be published after commit")
    public void anWorkloadEventShouldBePublishedAfterCommit(String actionType) {
        TrainerWorkloadRequest message = receiveWorkloadMessage();
        Assertions.assertNotNull(message);
        Assertions.assertEquals(ActionType.valueOf(actionType), message.getActionType());
        context.setLastWorkloadMessage(message);
    }

    @Then("the published workload event should refer to the trainer and training")
    public void thePublishedWorkloadEventShouldReferToTheTrainerAndTraining() {
        Assertions.assertNotNull(context.getLastWorkloadMessage());
        Assertions.assertEquals(context.getTrainerUsername(), context.getLastWorkloadMessage().getTrainerUsername());
        Assertions.assertNotNull(context.getLastWorkloadMessage().getTrainingId());
        Assertions.assertTrue(context.getLastWorkloadMessage().getTrainingDuration() > 0);
    }

    @Then("no training should be persisted")
    public void noTrainingShouldBePersisted() {
        Assertions.assertEquals(0, trainingRepository.count());
    }

    @Then("no workload event should be published")
    public void noWorkloadEventShouldBePublished() {
        Assertions.assertNull(receiveWorkloadMessage());
    }

    @Then("the response should mention validation failure")
    public void theResponseShouldMentionValidationFailure() {
        String body = context.getLastResponseBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.contains("trainingName") || body.contains("Validation Failed") || body.contains("must not be blank"));
    }

    @Then("the response should mention forbidden access")
    public void theResponseShouldMentionForbiddenAccess() {
        String body = context.getLastResponseBody();
        Assertions.assertNotNull(body);
        Assertions.assertTrue(body.contains("Access is denied") || body.contains("Forbidden") || body.contains("Authenticated user does not match"));
    }

    @Then("the registered trainer should remain active")
    public void theRegisteredTrainerShouldRemainActive() {
        boolean active = userRepository.findByUsername(context.getTrainerUsername())
                .orElseThrow()
                .getIsActive();
        Assertions.assertTrue(active);
    }

    private TrainerWorkloadRequest receiveWorkloadMessage() {
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            Queue queue = jmsContext.createQueue(workloadQueueName);
            Message message = jmsContext.createConsumer(queue).receive(3000);
            if (message == null) {
                return null;
            }
            String json = message.getBody(String.class);
            Map<?, ?> payload = objectMapper.readValue(json, Map.class);
            return WorkloadMessageMapper.fromMap(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to receive workload event", ex);
        }
    }
}
