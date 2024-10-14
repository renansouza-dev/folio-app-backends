package com.renansouza.folio.transactions;

import com.renansouza.folio.transactions.config.RabbitMQConfig;
import com.renansouza.folio.transactions.models.AccountsNotification;
import com.renansouza.folio.transactions.models.TransactionsMapper;
import com.renansouza.folio.transactions.models.TransactionsOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static com.renansouza.folio.transactions.TransactionsUtils.getAmount;
import static com.renansouza.folio.transactions.TransactionsUtils.getEntities;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionsNotificationTest {

    private static final String EXCHANGE = "test-exchange";
    private static final String ROUTING_KEY = "test-routingKey";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQConfig rabbitMQConfig;

    @InjectMocks
    private TransactionsNotification transactionsNotification;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rabbitMQConfig.getExchangeName()).thenReturn(EXCHANGE);
        when(rabbitMQConfig.getRoutingKey()).thenReturn(ROUTING_KEY);
    }

    @Test
    void testSendAccountQueueMessage_SaveBuyTransaction() {
        // Arrange
        var operation = TransactionsOperation.SAVE;
        var entity = getEntities(1).getFirst();
        var amount = getAmount(entity, operation);

        // Act
        transactionsNotification.sendAccountQueueMessage(TransactionsMapper.entityToDto(operation, entity));

        // Assert
        var expectedMessage = new AccountsNotification(entity.getBroker(), amount);
        verify(rabbitTemplate).convertAndSend(EXCHANGE, ROUTING_KEY, expectedMessage);
    }

    @Test
    void testSendAccountQueueMessage_DeleteSellTransaction() {
        // Arrange
        TransactionsOperation operation = TransactionsOperation.DELETE;
        var entity = getEntities(1).getFirst();
        var amount = getAmount(entity, operation);

        // Act
        transactionsNotification.sendAccountQueueMessage(TransactionsMapper.entityToDto(operation, entity));

        // Assert
        var expectedMessage = new AccountsNotification(entity.getBroker(), amount);
        verify(rabbitTemplate).convertAndSend(EXCHANGE, ROUTING_KEY, expectedMessage);
    }

}