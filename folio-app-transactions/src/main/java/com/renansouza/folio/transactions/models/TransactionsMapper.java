package com.renansouza.folio.transactions.models;

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
}
