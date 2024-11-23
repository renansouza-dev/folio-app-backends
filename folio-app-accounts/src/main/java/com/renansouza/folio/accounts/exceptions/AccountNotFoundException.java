package com.renansouza.folio.accounts.exceptions;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(@NotBlank UUID id) {
        super(String.format("The provided account id %s was not found", id));
    }

}