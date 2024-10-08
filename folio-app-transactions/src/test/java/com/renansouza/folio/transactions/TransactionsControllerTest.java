package com.renansouza.folio.transactions;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renansouza.folio.transactions.exceptions.TransactionNotFoundException;
import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.renansouza.folio.transactions.TransactionsUtils.getFailureRequest;
import static com.renansouza.folio.transactions.TransactionsUtils.getRequests;
import static com.renansouza.folio.transactions.TransactionsUtils.getResponses;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Unit")
@WebMvcTest(TransactionsController.class)
class TransactionsControllerTest {

    private static final String PATH = "/v1/transactions";
    private static final VerificationMode ONCE = times(1);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    TransactionsService service;

    @Test
    @DisplayName("get zero transactions without filters.")
    void getZeroTransactions() throws Exception {
        // Given

        // Then
        mvc.perform(get(PATH)).andExpect(status().isNoContent());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(any(), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("get zero transactions filtering by wrong broker.")
    void getZeroTransactionsByBroker() throws Exception {
        // Given
        String broker = "BROKER";
        var byBrokers = getResponses(10).stream().filter(transaction -> broker.equals(transaction.broker())).toList();
        when(service.find(eq(broker), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(byBrokers));

        // Then
        mvc.perform(get(PATH).param("broker", broker).accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(eq(broker), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("get all transactions without filters.")
    void getTransactions() throws Exception {
        // Given
        when(service.find(any(), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(getResponses(3)));

        // Then
        mvc.perform(get(PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(3)))
                .andExpect(jsonPath("$.page.totalPages", is(1)))
                .andExpect(jsonPath("$.content", hasSize(3)));

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(any(), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("get all transactions filtering by broker.")
    void getTransactionsByBroker() throws Exception {
        // Given
        List<TransactionsResponse> responseList = getResponses(10);
        var broker = responseList.getFirst().broker();
        var byBrokers = responseList.stream().filter(transaction -> broker.equals(transaction.broker())).toList();

        when(service.find(eq(broker), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(byBrokers));

        // Then
        mvc.perform(get(PATH).param("broker", broker).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(eq(broker), any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("get all transactions filtering by asset.")
    void getTransactionsByAsset() throws Exception {
        // Given
        List<TransactionsResponse> responseList = getResponses(10);
        var asset = responseList.getFirst().asset();
        var byAssets = getResponses(5).stream().filter(transaction -> asset.equals(transaction.asset())).toList();

        when(service.find(any(), eq(asset), any(PageRequest.class))).thenReturn(new PageImpl<>(byAssets));

        // Then
        mvc.perform(get(PATH).param("asset", asset).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(byAssets.size())))
                .andExpect(jsonPath("$.page.totalPages", is(1)))
                .andExpect(jsonPath("$.content", hasSize(byAssets.size())));

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(any(), eq(asset), any(PageRequest.class));
    }

    @Test
    @DisplayName("add a new transaction successfully")
    void  addTransaction() throws Exception {
        // Given

        // Then
        mvc.perform(post(PATH).content(mapper.writeValueAsString(getRequests(1).getFirst())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).save(any(TransactionsRequest.class));
    }

    @Test
    @DisplayName("failed to add a new transaction successfully")
    void  failedToAddTransaction() throws Exception {
        // Given

        // The
        mvc.perform(post(PATH).content(getFailureRequest()).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path", is(PATH)))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.error", hasToString(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.message", stringContainsInOrder("date: Date cannot be null.")));

        // Verify that the repository was called with the correct arguments
        verify(service, never()).save(any(TransactionsRequest.class));
    }

    @Test
    @DisplayName("delete a transaction by providing an id.")
    void deleteTransaction() throws Exception {
        // Given
        doNothing().when(service).delete(anyLong());

        // Then
        mvc.perform(delete(PATH + "/{id}", 1)).andExpect(status().isNoContent());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).delete(anyLong());
    }

    @Test
    @DisplayName("fail to delete a transaction by providing an id due to not found.")
    void failToDeleteTransaction() throws Exception {
        // Given
        var id = 1;
        doThrow(new TransactionNotFoundException(id)).when(service).delete(anyLong());

        // Then
        var message = String.format("The provided id %s transaction was not found", id);
        mvc.perform(delete(PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", is(PATH + "/" + id)))
                .andExpect(jsonPath("$.message", is(message)))
                .andExpect(jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())));

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).delete(anyLong());
    }

}