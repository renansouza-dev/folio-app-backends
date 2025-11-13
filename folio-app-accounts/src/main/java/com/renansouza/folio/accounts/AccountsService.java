package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.exceptions.AccountAlreadyExistsException;
import com.renansouza.folio.accounts.exceptions.AccountNotFoundException;
import com.renansouza.folio.accounts.models.AccountsMapper;
import com.renansouza.folio.accounts.models.AccountsNotification;
import com.renansouza.folio.accounts.models.AccountsRequest;
import com.renansouza.folio.accounts.models.AccountsResponse;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountsService {

  private final AccountsRepository repository;

  @Cacheable(
      value = "accounts",
      unless = "#result.isEmpty()",
      condition = "#page.pageSize <= 100",
      key = "T(String).format('%s_%d_%d', #broker != null ? #broker : 'ALL', #page.pageNumber, #page.pageSize)"
  )
  Page<AccountsResponse> find(String broker, PageRequest page) {
    return Strings.isEmpty(broker)
        ? repository.findAllAccounts(page)
        : repository.findByBroker(broker, page);
  }

  @CachePut(value = "accounts", key = "#request.broker")
  void save(AccountsRequest request) {
    if (repository.existsByBroker(request.broker())) {
      throw new AccountAlreadyExistsException(request.broker());
    }

    repository.save(AccountsMapper.dtoToEntity(request));
  }

  @CachePut(value = "accounts", key = "#notification.broker")
  void updateAccountAmount(AccountsNotification notification) {
    if (!repository.existsById(notification.broker())) {
      throw new AccountNotFoundException(notification.broker());
    }

    repository.updateAmountById(notification.broker(), notification.amount());
  }

  @CachePut(value = "accounts", key = "#request.broker")
  void update(UUID id, @Valid AccountsRequest request) {
    var account = repository.findById(id);
    if (account.isEmpty()) {
      throw new AccountNotFoundException(id);
    }

    account.filter(acc -> !acc.getBroker().equals(request.broker()) || !acc.getAmount().equals(request.amount()))
        .ifPresent(acc -> {
          acc.setBroker(request.broker());
          acc.setAmount(request.amount());

          repository.save(acc);
        });
  }
}