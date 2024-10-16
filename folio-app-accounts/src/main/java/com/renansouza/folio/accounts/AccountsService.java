package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.models.AccountsNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountsService {

    private final AccountsRepository repository;

    void updateAccountAmount(AccountsNotification notification) {
        repository.updateAmountById(notification.account(), notification.amount());
    }

}