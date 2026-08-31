package com.epam.gymcrmspringboot.bdd;

import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BddTestBeans {

    @Bean
    public BddTestContext bddTestContext() {
        return new BddTestContext();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService embeddedBrokerService() throws Exception {
        BrokerService brokerService = new BrokerService();
        brokerService.setBrokerName("embedded-broker");
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.setUseShutdownHook(false);
        return brokerService;
    }
}
