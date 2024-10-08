package com.renansouza.folio.transactions;

import java.util.Objects;

import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/transactions")
@Tag(name = "transactions", description = "Transactions management APIs")
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Payload invalid"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "500", description = "Something went wrong"),
})
public class TransactionsController {

    private final TransactionsService service;

    @Operation(summary = "Get a list of transaction")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Found a zero or more transactions",
                    content = { @Content(schema = @Schema(implementation = TransactionsRequest[].class)) }),
            @ApiResponse(
                    responseCode = "204",
                    description = "Found a zero transactions")
    })
    @GetMapping
    @Cacheable(value = "transactionsCache", key = "#root.methodName + '_' + #asset + '_' + #broker + '_' + #pageSize + '_' + #pageNumber + '_' + #property + '_' + #direction")
    ResponseEntity<Page<TransactionsResponse>> getTransactions(@RequestParam(required = false) String broker,
                                                               @RequestParam(required = false) String asset,
                                                               @RequestParam(required = false, defaultValue = "20") String pageSize,
                                                               @RequestParam(required = false, defaultValue = "0") String pageNumber,
                                                               @RequestParam(required = false, defaultValue = "date") String property,
                                                               @RequestParam(required = false, defaultValue = "asc") String direction) {
        var sort = Sort.by(Sort.Direction.fromString(direction), property);
        var page = PageRequest.of(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sort);

        var transactions = service.find(broker, asset, page);
        if (Objects.isNull(transactions) || transactions.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new transaction")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Create a new transaction",
                    content = { @Content(schema = @Schema(implementation = TransactionsRequest.class)) })
    })
    @CacheEvict(value = "transactionsCache", allEntries = true)
    void addTransaction(@Valid @RequestBody TransactionsRequest request) {
        service.save(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a transaction using its id")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Delete transaction")})
    @CacheEvict(value = "transactionsCache", allEntries = true)
    @Scheduled(fixedRateString = "${application.caching.spring.cacheTTL}")
    void deleteTransaction(@PathVariable("id") Long id) {
        service.delete(id);
    }

}