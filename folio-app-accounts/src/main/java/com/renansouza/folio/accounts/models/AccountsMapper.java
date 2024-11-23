package com.renansouza.folio.accounts.models;

public class AccountsMapper {

    private AccountsMapper() {}

    public static AccountsEntity dtoToEntity(AccountsRequest request) {
        var entity = new AccountsEntity();
        entity.setBroker(request.broker());
        entity.setAmount(request.amount());

        return entity;
    }
}