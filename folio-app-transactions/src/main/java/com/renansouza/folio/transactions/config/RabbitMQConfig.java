package com.renansouza.folio.transactions.config;

import lombok.Getter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Value("${rabbitmq.queue.name}")
  private String queueName;

  @Value("${rabbitmq.queue.dlq:#{null}}")
  private String dlqName;

  @Getter
  @Value("${rabbitmq.exchange.name}")
  private String exchangeName;

  @Getter
  @Value("${rabbitmq.routing-key}")
  private String routingKey;

  @Bean
  MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public Queue accountsQueue() {
    var builder = QueueBuilder.durable(queueName).withArgument("x-dead-letter-exchange", "");

    if (dlqName != null && !dlqName.isEmpty()) {
      builder.withArgument("x-dead-letter-routing-key", dlqName);
    }

    return builder.build();
  }

  @Bean
  public Queue accountsDeadLetterQueue() {
    if (dlqName != null && !dlqName.isEmpty()) {
      return QueueBuilder.durable(dlqName).build();
    }
    return null;
  }

  @Bean
  public TopicExchange accountsExchange() {
    return new TopicExchange(exchangeName);
  }

  @Bean
  public Binding binding() {
    return BindingBuilder.bind(accountsQueue()).to(accountsExchange()).with(routingKey);
  }

}