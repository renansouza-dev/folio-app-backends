package com.renansouza.folio.accounts;

import java.math.BigDecimal;
import java.util.UUID;

import com.renansouza.folio.accounts.models.AccountsNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        // Given
        var notification = new AccountsNotification(UUID.randomUUID(), BigDecimal.ONE);

        // When
        listener.getAccountUpdate(notification);

        // Then
        verify(service, times(1)).updateAccountAmount(notification);
    }
}