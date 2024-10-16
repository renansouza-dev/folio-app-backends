package com.renansouza.folio.accounts.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountsNotification(
        @JsonProperty("account") UUID account,
        @JsonProperty("amount") BigDecimal amount) implements Serializable {}