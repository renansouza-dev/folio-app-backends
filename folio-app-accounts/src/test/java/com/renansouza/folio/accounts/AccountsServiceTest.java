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
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Tag("Unit")
class AccountsServiceTest {

    private static final VerificationMode ONCE = times(1);

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

        // When
        service.updateAccountAmount(notification);

        // Then
        verify(repository, ONCE).updateAmountById(notification.account(), notification.amount());
    }
}