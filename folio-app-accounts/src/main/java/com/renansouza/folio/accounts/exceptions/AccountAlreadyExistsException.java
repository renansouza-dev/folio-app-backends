package com.renansouza.folio.accounts.exceptions;

public class AccountAlreadyExistsException extends RuntimeException {

    public AccountAlreadyExistsException(String broker) {
        super(String.format("The provided broker %s already exists.", broker));
    }

}