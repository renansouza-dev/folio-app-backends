package com.renansouza.folio.transactions;

import java.util.Objects;

import com.renansouza.folio.transactions.exceptions.TransactionNotFoundException;
import com.renansouza.folio.transactions.models.TransactionsMapper;
import com.renansouza.folio.transactions.models.TransactionsOperation;
import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TransactionsService {

    private final TransactionsRepository repository;
    private final TransactionsNotification notification;

    Page<TransactionsResponse> find(String broker, String asset, PageRequest page) {
        if (Objects.nonNull(broker)) {
            return repository.findAllTransactionsByBroker(broker, page);
        }

        if (Objects.nonNull(asset)) {
            return repository.findAllTransactionsByAsset(asset, page);
        }

        return repository.findAllTransactions(page);
    }

    void save(TransactionsRequest request) {
        var transaction = repository.save(TransactionsMapper.dtoToEntity(request));

        notification.sendAccountQueueMessage(TransactionsMapper.entityToDto(TransactionsOperation.SAVE, transaction));
    }

    void delete(Long id) {
        var transaction = repository.findById(id).orElseThrow(() -> new TransactionNotFoundException(id));

        repository.delete(transaction);
        notification.sendAccountQueueMessage(TransactionsMapper.entityToDto(TransactionsOperation.DELETE, transaction));
    }



}