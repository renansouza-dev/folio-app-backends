package com.renansouza.folio.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renansouza.folio.transactions.config.RabbitMQConfig;
import com.renansouza.folio.transactions.models.TransactionsAccountNotification;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionsNotification {

    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitTemplate rabbitTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public void sendAccountQueueMessage(TransactionsAccountNotification notification) {
        var message = objectMapper.writeValueAsString(notification);
        rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getRoutingKey(), message);
    }

}