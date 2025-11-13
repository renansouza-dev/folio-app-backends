package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.models.AccountsNotification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Tag("Unit")
class AccountsListenerTest {

  @InjectMocks
  private AccountsListener listener;

  @Mock
  private AccountsService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getAccountUpdate() {
    var notification = new AccountsNotification(UUID.randomUUID(), BigDecimal.ONE);

    listener.getAccountUpdate(notification);

    verify(service, times(1)).updateAccountAmount(notification);
  }

  @Test
  void shouldThrowExceptionWhenServiceFails() {
    var notification = new AccountsNotification(UUID.randomUUID(), BigDecimal.ONE);

    doThrow(new RuntimeException("Service failed")).when(service).updateAccountAmount(notification);

    assertThrows(RuntimeException.class, () -> listener.getAccountUpdate(notification));
    verify(service, times(1)).updateAccountAmount(notification);
  }
}