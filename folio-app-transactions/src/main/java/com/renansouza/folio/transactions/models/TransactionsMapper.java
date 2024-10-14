package com.renansouza.folio.transactions.models;

import java.math.BigDecimal;

public class TransactionsMapper {

    private TransactionsMapper() {}

    public static TransactionsEntity dtoToEntity(TransactionsRequest request) {
        var entity = new TransactionsEntity();
        entity.setFee(request.fee());
        entity.setDate(request.date());
        entity.setType(request.type());
        entity.setAsset(request.asset());
        entity.setPrice(request.price());
        entity.setBroker(request.broker());
        entity.setQuantity(request.quantity());

        return entity;
    }

    public static AccountsNotification entityToDto(TransactionsOperation operation, TransactionsEntity entity) {
        var total = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        var totalWithFee = TransactionType.BUY.equals(entity.getType()) ? total.add(entity.getFee()) : total.subtract(entity.getFee());
        var amount = shouldNegateTotal(entity.getType(), operation)
                ? totalWithFee.multiply(BigDecimal.valueOf(-1))
                : totalWithFee;

        return new AccountsNotification(entity.getBroker(), amount);
    }

    private static boolean shouldNegateTotal(TransactionType type, TransactionsOperation operation) {
        return (TransactionType.BUY.equals(type) && TransactionsOperation.SAVE.equals(operation)) ||
                (TransactionType.SELL.equals(type) && TransactionsOperation.DELETE.equals(operation));
    }

}