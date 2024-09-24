package com.renansouza.folio.transactions.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionsResponse(Long id, LocalDate date, TransactionType type, String asset, BigDecimal price, int quantity, BigDecimal fee, String broker) { }