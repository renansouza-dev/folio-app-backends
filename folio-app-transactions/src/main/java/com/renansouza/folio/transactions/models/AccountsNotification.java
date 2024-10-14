package com.renansouza.folio.transactions.models;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountsNotification(
        @JsonProperty("account") String account,
        @JsonProperty("amount") BigDecimal amount) implements Serializable {}