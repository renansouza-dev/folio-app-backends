package com.renansouza.folio.transactions;

import com.renansouza.folio.transactions.exceptions.TransactionNotFoundException;
import com.renansouza.folio.transactions.models.TransactionsMapper;
import com.renansouza.folio.transactions.models.TransactionsOperation;
import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;

import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TransactionsService {

  private final TransactionsRepository repository;
  private final TransactionsNotification notification;

  @Cacheable(
      value = "transactions",
      unless = "#result.isEmpty()",
      condition = "#page.pageSize <= 100",
      key = "T(String).format('%s_%d_%d', #broker != null ? #broker : #asset, #page.pageNumber, #page.pageSize)"
  )
  Page<TransactionsResponse> find(UUID broker, String asset, PageRequest page) {
    if (Objects.nonNull(broker)) {
      return repository.findAllTransactionsByBroker(broker, page);
    }

    if (Objects.nonNull(asset)) {
      return repository.findAllTransactionsByAsset(asset, page);
    }

    return repository.findAllTransactions(page);
  }

  @CachePut(value = "transactions", key = "#request.broker")
  void save(TransactionsRequest request) {
    var transaction = repository.save(TransactionsMapper.dtoToEntity(request));

    notification.sendAccountQueueMessage(
        TransactionsMapper.entityToDto(TransactionsOperation.SAVE, transaction));
  }

  @CacheEvict(value = "transactions", key = "#id")
  void delete(Long id) {
    var transaction = repository.findById(id)
        .orElseThrow(() -> new TransactionNotFoundException(id));

    repository.delete(transaction);
    notification.sendAccountQueueMessage(
        TransactionsMapper.entityToDto(TransactionsOperation.DELETE, transaction));
  }

}