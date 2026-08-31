package com.epam.gymcrmspringboot.bdd;

import com.epam.gymcrmspringboot.model.TrainingTypeEntity;
import com.epam.gymcrmspringboot.repository.TraineeRepository;
import com.epam.gymcrmspringboot.repository.TrainerRepository;
import com.epam.gymcrmspringboot.repository.TrainingRepository;
import com.epam.gymcrmspringboot.repository.TrainingTypeRepository;
import com.epam.gymcrmspringboot.repository.UserRepository;
import io.cucumber.java.Before;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import org.springframework.beans.factory.annotation.Value;
public class CommonBddHooks {

    private final BddTestContext context;
    private final ConnectionFactory connectionFactory;
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UserRepository userRepository;
    private final String workloadQueue;
    private final String invalidDlq;

    public CommonBddHooks(
            BddTestContext context,
            ConnectionFactory connectionFactory,
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository,
            UserRepository userRepository,
            @Value("${app.messaging.queue.trainer-workload}") String workloadQueue,
            @Value("${app.messaging.queue.trainer-workload-invalid-dlq}") String invalidDlq) {
        this.context = context;
        this.connectionFactory = connectionFactory;
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.userRepository = userRepository;
        this.workloadQueue = workloadQueue;
        this.invalidDlq = invalidDlq;
    }

    @Before
    public void resetContext() {
        context.clear();
        trainingRepository.deleteAll();
        traineeRepository.deleteAll();
        trainerRepository.deleteAll();
        userRepository.deleteAll();
        ensureTrainingTypesExist();
        purgeQueue(workloadQueue);
        purgeQueue(invalidDlq);
    }

    private void ensureTrainingTypesExist() {
        if (trainingTypeRepository.count() > 0) {
            return;
        }

        trainingTypeRepository.save(TrainingTypeEntity.builder().trainingTypeName("YOGA").build());
        trainingTypeRepository.save(TrainingTypeEntity.builder().trainingTypeName("CARDIO").build());
        trainingTypeRepository.save(TrainingTypeEntity.builder().trainingTypeName("STRENGTH").build());
        trainingTypeRepository.save(TrainingTypeEntity.builder().trainingTypeName("PILATES").build());
        trainingTypeRepository.save(TrainingTypeEntity.builder().trainingTypeName("CROSSFIT").build());
    }

    private void purgeQueue(String queueName) {
        try (JMSContext jmsContext = connectionFactory.createContext()) {
            Queue queue = jmsContext.createQueue(queueName);
            JMSConsumer consumer = jmsContext.createConsumer(queue);
            while (consumer.receiveNoWait() != null) {
                // keep draining
            }
        }
    }
}
