package com.renansouza.folio.accounts.config;

import lombok.Generated;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Generated
@Configuration
public class RabbitMQConfig {

    @Bean
    MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}