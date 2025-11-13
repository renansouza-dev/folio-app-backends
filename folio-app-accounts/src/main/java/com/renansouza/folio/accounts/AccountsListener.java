package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.models.AccountsNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountsListener {

  private final AccountsService service;

  @RabbitListener(queues = "${rabbitmq.queue.name}")
  void getAccountUpdate(@Payload AccountsNotification notification) {
    log.debug("Received account notification: {}", notification);
    try {
      service.updateAccountAmount(notification);
      log.debug("Account updated successfully: {}", notification.broker());
    } catch (Exception e) {
      log.error("Error processing account notification: {}", notification, e);
      throw e;
    }
  }

}