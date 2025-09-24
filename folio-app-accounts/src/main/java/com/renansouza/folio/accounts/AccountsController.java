package com.renansouza.folio.accounts;

import com.renansouza.folio.accounts.models.AccountsRequest;
import com.renansouza.folio.accounts.models.AccountsResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/accounts")
@Tag(name = "accounts", description = "Accounts management APIs")
@ApiResponses(value = {
    @ApiResponse(responseCode = "400", description = "Payload invalid"),
    @ApiResponse(responseCode = "404", description = "Account not found"),
    @ApiResponse(responseCode = "500", description = "Something went wrong"),
})
public class AccountsController {

  private final AccountsService service;

  @Operation(summary = "Get a list of accounts")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Found a one or more accounts",
          content = {@Content(schema = @Schema(implementation = AccountsResponse[].class))}),
      @ApiResponse(
          responseCode = "204",
          description = "Found zero accounts")
  })
  @GetMapping
  @Cacheable(value = "accountsCache", key = "#root.methodName + #broker + '_' + #pageSize + '_' + #pageNumber + '_' + #property + '_' + #direction")
  ResponseEntity<Page<AccountsResponse>> getAccounts(@RequestParam(required = false) String broker,
      @RequestParam(required = false, defaultValue = "20") String pageSize,
      @RequestParam(required = false, defaultValue = "0") String pageNumber,
      @RequestParam(required = false, defaultValue = "asc") String direction,
      @RequestParam(required = false, defaultValue = "broker") String property) {
    var sort = Sort.by(Sort.Direction.fromString(direction), property);
    var page = PageRequest.of(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sort);

    var accounts = service.find(broker, page);
    if (accounts.isEmpty()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(accounts);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a new account")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "201",
          description = "Create a new account",
          content = {@Content(schema = @Schema(implementation = AccountsResponse.class))})
  })
  @CacheEvict(value = "accountsCache", allEntries = true)
  void addAccount(@Valid @RequestBody AccountsRequest request) {
    service.save(request);
  }

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Update an account")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "204",
          description = "Update an account",
          content = {@Content(schema = @Schema(implementation = AccountsResponse.class))})
  })
  @CacheEvict(value = "accountsCache", allEntries = true)
  void updateAccount(@PathVariable UUID id, @Valid @RequestBody AccountsRequest request) {
    service.update(id, request);
  }
}