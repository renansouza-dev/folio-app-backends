package com.renansouza.folio.accounts.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountsNotification(
    @JsonProperty("account") UUID account,
    @JsonProperty("amount") BigDecimal amount) implements Serializable {

}