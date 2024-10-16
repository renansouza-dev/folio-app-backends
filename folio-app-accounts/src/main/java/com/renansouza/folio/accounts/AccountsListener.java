package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.models.AccountsNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountsListener {

    private final AccountsService service;

    @RabbitListener(queues = "${rabbitmq.queue-name:accounts}")
    void getAccountUpdate(@Payload AccountsNotification notification) {
        service.updateAccountAmount(notification);
    }

}