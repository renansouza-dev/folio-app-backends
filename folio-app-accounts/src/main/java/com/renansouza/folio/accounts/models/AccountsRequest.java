package com.renansouza.folio.accounts.models;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AccountsRequest(
        @NotNull(message = "Broker cannot be null.")
        String broker,

        @PositiveOrZero(message = "Amount must be zero or positive number.")
        @NotNull(message = "Amount cannot be null.")
        BigDecimal amount) {

    public AccountsRequest(String broker, BigDecimal amount) {
        this.broker = broker.toUpperCase();
        this.amount = amount;
    }

}