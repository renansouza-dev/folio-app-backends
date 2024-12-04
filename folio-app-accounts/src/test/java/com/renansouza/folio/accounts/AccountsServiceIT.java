package com.renansouza.folio.accounts;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.renansouza.folio.accounts.models.AccountsNotification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.renansouza.folio.accounts.AccountUtils.getEntities;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Tag("Integration")
class AccountsServiceIT {

    private static final String QUEUE_NAME = "accounts";

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3-alpine");
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.0.2-management-alpine").withExposedPorts(5672);

    @Autowired
    AccountsRepository repository;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        rabbit.start();

        try {
            rabbit.execInContainer("bash", "-c", "rabbitmqadmin declare queue name=%s durable=true".formatted(QUEUE_NAME));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        rabbit.stop();
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getFirstMappedPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Test
    @DisplayName("update account amount after message posted on queue")
    void updateAccountAmount() throws InterruptedException {
        var entity = repository.save(getEntities(1).getFirst());

        sendMessage(entity.getId());

        Thread.sleep(5 * 1000);

        var updatedEntity = repository.findById(entity.getId());

        assertTrue(updatedEntity.isPresent());
        assertEquals(updatedEntity.get().getId(), entity.getId());
        assertEquals(updatedEntity.get().getBroker(), entity.getBroker());
        assertEquals(updatedEntity.get().getAmount(), entity.getAmount().add(BigDecimal.TEN));
    }

    private static void sendMessage(UUID brokerId) {
        var factory = new ConnectionFactory();
        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getFirstMappedPort());
        factory.setUsername(rabbit.getAdminUsername());
        factory.setPassword(rabbit.getAdminPassword());

        try (var connection = factory.newConnection();
             var channel = connection.createChannel()) {

            var notification = new AccountsNotification(brokerId, BigDecimal.TEN);
            var message = new ObjectMapper().writeValueAsString(notification);

            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}