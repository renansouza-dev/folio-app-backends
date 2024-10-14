package com.renansouza.folio.transactions;

import com.renansouza.folio.transactions.config.RabbitMQConfig;
import com.renansouza.folio.transactions.models.AccountsNotification;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionsNotification {

    private final RabbitMQConfig rabbitMQConfig;
    private final RabbitTemplate rabbitTemplate;

    @SneakyThrows
    public void sendAccountQueueMessage(AccountsNotification notification) {
        rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getRoutingKey(), notification);
    }

}