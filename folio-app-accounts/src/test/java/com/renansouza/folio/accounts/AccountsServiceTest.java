package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.exceptions.AccountAlreadyExistsException;
import com.renansouza.folio.accounts.exceptions.AccountNotFoundException;
import com.renansouza.folio.accounts.models.AccountsEntity;
import com.renansouza.folio.accounts.models.AccountsNotification;
import com.renansouza.folio.accounts.models.AccountsResponse;

import static com.renansouza.folio.accounts.AccountUtils.getEntities;
import static com.renansouza.folio.accounts.AccountUtils.getRequests;
import static com.renansouza.folio.accounts.AccountUtils.getResponses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@Tag("Unit")
class AccountsServiceTest {

  public static final VerificationMode ONCE = times(1);

  @Mock
  private AccountsRepository repository;

  @InjectMocks
  private AccountsService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void updateAccountAmount() {
    // Given
    var notification = new AccountsNotification(UUID.randomUUID(), BigDecimal.ONE);
    when(repository.existsById(notification.broker())).thenReturn(true);

    // When
    service.updateAccountAmount(notification);

    // Then
    verify(repository, ONCE).existsById(notification.broker());
    verify(repository, ONCE).updateAmountById(notification.broker(), notification.amount());
  }

  @Test
  void failToUpdateAccountAmount() {
    // Given
    var notification = new AccountsNotification(UUID.randomUUID(), BigDecimal.ONE);
    when(repository.existsById(notification.broker())).thenReturn(false);

    // When
    var accountAlreadyExistsException = assertThrows(AccountNotFoundException.class,
        () -> service.updateAccountAmount(notification));

    // Then
    var expectedMessage = String.format("The provided account id %s was not found", notification.broker());
    assertEquals(expectedMessage, accountAlreadyExistsException.getMessage());
    verify(repository, ONCE).existsById(notification.broker());
    verify(repository, never()).updateAmountById(notification.broker(), notification.amount());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void testFind(String broker) {
    // Given
    PageRequest pageRequest = PageRequest.of(0, 10);
    Page<AccountsResponse> expectedPage = new PageImpl<>(Collections.emptyList());

    when(repository.findAllAccounts(pageRequest)).thenReturn(expectedPage);

    // When
    Page<AccountsResponse> result = service.find(broker, pageRequest);

    // Then
    assertEquals(expectedPage, result);
    verify(repository, ONCE).findAllAccounts(pageRequest);
    verify(repository, never()).findByBroker(anyString(), any());
  }

  @Test
  void testFindBroker() {
    // Given
    var entities = getResponses(2);
    var broker = entities.getLast().broker();

    PageRequest pageRequest = PageRequest.of(1, 10);
    Page<AccountsResponse> expectedPage = new PageImpl<>(entities);

    when(repository.findByBroker(broker, pageRequest)).thenReturn(expectedPage);

    // When
    Page<AccountsResponse> result = service.find(broker, pageRequest);

    // Then
    assertEquals(expectedPage, result);
    verify(repository, never()).findAllAccounts(pageRequest);
    verify(repository, ONCE).findByBroker(eq(broker), any());
  }

  @Test
  void testSave() {
    // Given
    var request = getRequests(1).getFirst();

    when(repository.existsByBroker(request.broker())).thenReturn(false);

    // When
    service.save(request);

    // Then
    verify(repository, ONCE).existsByBroker(request.broker());
    verify(repository, ONCE).save(any(AccountsEntity.class));
  }

  @Test
  void failToTestSave() {
    // Given
    var request = getRequests(1).getFirst();
    when(repository.existsByBroker(anyString())).thenReturn(true);

    // When
    assertThrows(AccountAlreadyExistsException.class, () -> service.save(request));
    verify(repository, never()).save(any(AccountsEntity.class));
    verify(repository, ONCE).existsByBroker(anyString());
  }

  @Test
  void updateAccount() {
    // Given
    var id = UUID.randomUUID();
    var request = getRequests(1).getFirst();
    var entity = getEntities(1).getFirst();

    when(repository.findById(id)).thenReturn(Optional.of(entity));

    // When
    service.update(id, request);

    // Then
    verify(repository, ONCE).findById(id);
    verify(repository, ONCE).save(any(AccountsEntity.class));
  }

  @Test
  void failToUpdateAccount() {
    // Given
    var id = UUID.randomUUID();
    var request = getRequests(1).getFirst();
    when(repository.findById(id)).thenReturn(Optional.empty());

    // When
    var accountNotFoundException = assertThrows(AccountNotFoundException.class,
        () -> service.update(id, request));

    // Then
    var expectedMessage = String.format("The provided account id %s was not found", id);
    assertEquals(expectedMessage, accountNotFoundException.getMessage());
    verify(repository, ONCE).findById(id);
    verify(repository, never()).save(any(AccountsEntity.class));
  }

}