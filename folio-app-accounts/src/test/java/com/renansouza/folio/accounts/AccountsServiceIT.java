package com.renansouza.folio.accounts;

import com.rabbitmq.client.ConnectionFactory;
import com.renansouza.folio.accounts.models.AccountsNotification;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.renansouza.folio.accounts.AccountUtils.getEntities;
import static org.awaitility.Awaitility.await;
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

    private static final ConnectionFactory factory = new ConnectionFactory();

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        rabbit.start();

        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getFirstMappedPort());
        factory.setUsername(rabbit.getAdminUsername());
        factory.setPassword(rabbit.getAdminPassword());

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
    void updateAccountAmount() {
        var savedEntity = repository.save(getEntities(1).getFirst());

        sendMessage(savedEntity.getId());

        await().atMost(1, TimeUnit.SECONDS).until(this::isMessageConsumed);

        var updatedEntity = repository.findById(savedEntity.getId());

        assertTrue(updatedEntity.isPresent());
        assertEquals(updatedEntity.get().getId(), savedEntity.getId());
        assertEquals(updatedEntity.get().getBroker(), savedEntity.getBroker());
        assertEquals(updatedEntity.get().getAmount(), savedEntity.getAmount().add(BigDecimal.TEN));
    }

    private static void sendMessage(UUID brokerId) {
        try (var connection = factory.newConnection();
             var channel = connection.createChannel()) {

            var notification = new AccountsNotification(brokerId, BigDecimal.TEN);
            var message = new ObjectMapper().writeValueAsString(notification);

            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isMessageConsumed() {
        try (var connection = factory.newConnection();
             var channel = connection.createChannel()) {

            return channel.basicGet(QUEUE_NAME, false) == null;
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}