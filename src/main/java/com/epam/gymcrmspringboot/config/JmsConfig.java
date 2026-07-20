package com.epam.gymcrmspringboot.config;

import tools.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
public class JmsConfig {

    @Bean
    public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        return new MessageConverter() {
            @Override
            public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
                try {
                    TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(object));
                    message.setStringProperty("_type", object.getClass().getSimpleName());
                    return message;
                } catch (JMSException e) {
                    throw e;
                } catch (Exception e) {
                    throw new MessageConversionException("Failed to serialize JMS payload", e);
                }
            }

            @Override
            public Object fromMessage(Message message) throws JMSException, MessageConversionException {
                throw new UnsupportedOperationException("This service is a producer only");
            }
        };
    }
}
