package id.ac.ui.cs.advprog.papikos.rentals.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    void testRentalTopicExchangeBean() {
        TopicExchange topicExchange = rabbitMQConfig.rentalTopicExchange();
        assertNotNull(topicExchange);
        assertEquals(RabbitMQConfig.TOPIC_EXCHANGE_NAME, topicExchange.getName());
    }

    @Test
    void testJsonMessageConverterBean() {
        MessageConverter messageConverter = rabbitMQConfig.jsonMessageConverter();
        assertNotNull(messageConverter);
        assertTrue(messageConverter instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void testRabbitTemplateBean() {
        ConnectionFactory mockConnectionFactory = mock(ConnectionFactory.class);
        RabbitTemplate rabbitTemplate = rabbitMQConfig.rabbitTemplate(mockConnectionFactory);

        assertNotNull(rabbitTemplate);
        assertEquals(mockConnectionFactory, rabbitTemplate.getConnectionFactory());
        assertTrue(rabbitTemplate.getMessageConverter() instanceof Jackson2JsonMessageConverter);
    }

    @Test
    void testConstants() {
        assertEquals("rental.topic", RabbitMQConfig.TOPIC_EXCHANGE_NAME);
        assertEquals("rental.created", RabbitMQConfig.ROUTING_KEY_RENTAL_CREATED);
        assertEquals("rental.approved", RabbitMQConfig.ROUTING_KEY_RENTAL_APPROVED);
    }
}