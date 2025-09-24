package com.renansouza.folio.transactions.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionsResponse(Long id, LocalDate date, TransactionType type, String asset,
                                   BigDecimal price, int quantity, BigDecimal fee, UUID broker) {

}