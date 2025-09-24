package com.renansouza.folio.accounts.exceptions;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class AccountNotFoundException extends RuntimeException {

  public AccountNotFoundException(@NotBlank UUID id) {
    super(String.format("The provided account id %s was not found", id));
  }

}