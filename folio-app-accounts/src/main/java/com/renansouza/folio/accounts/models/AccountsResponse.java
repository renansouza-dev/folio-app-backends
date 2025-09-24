package com.renansouza.folio.accounts.models;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountsResponse(UUID id, String broker, BigDecimal amount) {

}