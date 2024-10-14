package com.renansouza.folio.transactions;

import java.util.Optional;

import com.renansouza.folio.transactions.exceptions.TransactionNotFoundException;
import com.renansouza.folio.transactions.models.AccountsNotification;
import com.renansouza.folio.transactions.models.TransactionsEntity;
import com.renansouza.folio.transactions.models.TransactionsMapper;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.verification.VerificationMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import static com.renansouza.folio.transactions.TransactionsUtils.getEntities;
import static com.renansouza.folio.transactions.TransactionsUtils.getRequests;
import static com.renansouza.folio.transactions.TransactionsUtils.getResponses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
})
class TransactionsServiceTest {

    private static final int LIST_SIZE = 10;
    private static final PageRequest PAGE_REQUEST = PageRequest.of(0, LIST_SIZE);
    private static final VerificationMode ONCE = times(1);

    @Mock
    private TransactionsNotification notification;

    @Mock
    private TransactionsRepository repository;

    @InjectMocks
    private TransactionsService service;

    @Test
    void testFindAllTransactionsByBroker() {
        // Arrange
        var broker = "BROKER A";
        var responses = getResponses(LIST_SIZE).stream().filter(t -> t.broker().equals(broker)).toList();
        when(repository.findAllTransactionsByBroker(broker, PAGE_REQUEST)).thenReturn(new PageImpl<>(responses));

        // Act
        Page<TransactionsResponse> result = service.find(broker, null, PAGE_REQUEST);

        // Assert
        assertThat(result).isNotEmpty().hasSize(responses.size());
        verify(repository).findAllTransactionsByBroker(broker, PAGE_REQUEST);
    }

    @Test
    void testFindAllTransactionsByAsset() {
        // Arrange
        var asset = "ASSE1";
        var responses = getResponses(LIST_SIZE).stream().filter(t -> t.asset().equals(asset)).toList();
        when(repository.findAllTransactionsByAsset(asset, PAGE_REQUEST)).thenReturn(new PageImpl<>(responses));

        // Act
        Page<TransactionsResponse> result = service.find(null, asset, PAGE_REQUEST);

        // Assert
        assertThat(result).isNotEmpty().hasSize(responses.size());
        verify(repository).findAllTransactionsByAsset(asset, PAGE_REQUEST);
    }

    @Test
    void testFindAllTransactions() {
        // Arrange
        when(repository.findAllTransactions(PAGE_REQUEST)).thenReturn(new PageImpl<>(getResponses(LIST_SIZE)));

        // Act
        Page<TransactionsResponse> result = service.find(null, null, PAGE_REQUEST);

        // Assert
        assertThat(result).isNotEmpty().hasSize(LIST_SIZE);
        verify(repository, ONCE).findAllTransactions(PAGE_REQUEST);
    }

    @Test
    void testSaveTransaction() {
        // Arrange
        var request = getRequests(1).getFirst();
        var entity = TransactionsMapper.dtoToEntity(request);
        entity.setId(1L);

        when(repository.save(any(TransactionsEntity.class))).thenReturn(entity);

        // Act
        service.save(request);

        // Assert
        verify(repository, ONCE).save(any(TransactionsEntity.class));
        verify(notification, ONCE).sendAccountQueueMessage(any(AccountsNotification.class));

    }

    @Test
    void testDeleteTransaction() {
        // Arrange
        var entity = getEntities(1).getFirst();
        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

        // Act & Assert
        service.delete(entity.getId());

        verify(repository, ONCE).findById(entity.getId());
        verify(repository, ONCE).delete(any(TransactionsEntity.class));
        verify(notification, ONCE).sendAccountQueueMessage(any(AccountsNotification.class));
    }

    @Test
    void testDeleteTransaction_NotFound() {
        // Arrange
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TransactionNotFoundException.class, () -> service.delete(1L));
        verify(repository, ONCE).findById(anyLong());
        verify(repository, never()).delete(any(TransactionsEntity.class));
        verify(notification, never()).sendAccountQueueMessage(any(AccountsNotification.class));
    }
  
}