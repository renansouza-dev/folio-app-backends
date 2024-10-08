package com.renansouza.folio.transactions.exceptions;

import jakarta.validation.constraints.NotBlank;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(@NotBlank long id) {
        super(String.format("The provided id %s transaction was not found", id));
    }
}