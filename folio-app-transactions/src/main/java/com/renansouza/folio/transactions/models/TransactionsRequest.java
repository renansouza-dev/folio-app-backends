package com.renansouza.folio.transactions.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TransactionsRequest(
        @PastOrPresent(message = "Date must be today or in the past.")
        @NotNull(message = "Date cannot be null.")
        LocalDate date,

        @NotNull(message = "Type cannot be null.")
        TransactionType type,

        @NotNull(message = "Asset cannot be null.")
        @Size(min = 5, max = 6, message = "Asset must have at least 5 and max 6 characters.")
        String asset,

        @PositiveOrZero(message = "Price must be zero or positive number.")
        @NotNull(message = "price cannot be null.")
        BigDecimal price,

        @PositiveOrZero(message = "Quantity must be zero or positive number.")
        @NotNull(message = "Quantity cannot be null.")
        int quantity,

        @PositiveOrZero(message = "Fee must be zero or positive number.")
        @NotNull(message = "Fee cannot be null.")
        BigDecimal fee,

        @NotNull(message = "Broker cannot be null.")
        @Size(min = 5, max = 10, message = "Broker must have at least 5 and max 10 characters.")
        String broker) {

    public TransactionsRequest (LocalDate date, TransactionType type, String asset, BigDecimal price, int quantity, BigDecimal fee, String broker) {
        this.date = date;
        this.type = type;
        this.asset = asset.toUpperCase();
        this.price = price;
        this.quantity = quantity;
        this.fee = fee;
        this.broker = broker.toUpperCase();
    }

}