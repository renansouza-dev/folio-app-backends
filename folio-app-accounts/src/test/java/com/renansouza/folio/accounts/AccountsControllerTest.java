package com.renansouza.folio.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renansouza.folio.accounts.exceptions.AccountNotFoundException;
import com.renansouza.folio.accounts.models.AccountsRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static com.renansouza.folio.accounts.AccountUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("Unit")
@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    private static final String PATH = "/v1/accounts";
    private static final VerificationMode ONCE = times(1);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    AccountsService service;

    @Test
    @DisplayName("get all accounts with broker param")
    void getAllAccountsFromBrokerA() throws Exception {
        // Given
        var brokers = getResponses(1);
        var broker = brokers.getFirst().broker();

        // When
        when(service.find(eq(broker), any(PageRequest.class))).thenReturn(new PageImpl<>(brokers));

        // Then
        mvc.perform(get(PATH)
                        .param("broker", broker)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(brokers.size())))
                .andExpect(jsonPath("$.page.totalPages", is(1)))
                .andExpect(jsonPath("$.content", hasSize(brokers.size())));

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(eq(broker), any(PageRequest.class));

    }

    @Test
    @DisplayName("get all accounts with no broker as param")
    void getAllAccounts() throws Exception {
        // Given

        // When
        when(service.find(any(), any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        // Then
        mvc.perform(get(PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).find(any(), any(PageRequest.class));
    }

    @Test
    @DisplayName("add a new account successfully")
    void addNewAccount() throws Exception {
        // Given

        // Then
        mvc.perform(post(PATH)
                        .content(mapper.writeValueAsString(getRequests(1).getFirst()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).save(any(AccountsRequest.class));
    }

    @Test
    @DisplayName("failed to add a new account successfully")
    void  failedToAddAccount() throws Exception {
        // Given

        // The
        mvc.perform(post(PATH)
                        .content(getFailureRequest())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path", is(PATH)))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
                .andExpect(jsonPath("$.error", hasToString(HttpStatus.BAD_REQUEST.name())))
                .andExpect(jsonPath("$.message", stringContainsInOrder("amount: Amount cannot be null.")));

        // Verify that the repository was called with the correct arguments
        verify(service, never()).save(any(AccountsRequest.class));
    }

    @Test
    @DisplayName("add a new account successfully")
    void updateAccount() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var request = getRequests(1).getFirst();
        doNothing().when(service).update(id, request);

        // Then
        mvc.perform(put(PATH + "/{id}", id.toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).update(id, request);
    }

    @Test
    @DisplayName("failed to add a new account successfully")
    void  failedToUpdateAccount() throws Exception {
        // Given
        var id = UUID.randomUUID();
        var request = getRequests(1).getFirst();
        doThrow(new AccountNotFoundException(id)).when(service).update(id, request);


        // Then
        String exceptionMessage = String.format("The provided account id %s was not found", id);

        mvc.perform(put(PATH + "/{id}", id.toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path", is(PATH + "/" + id)))
                .andExpect(jsonPath("$.timestamp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
                .andExpect(jsonPath("$.error", hasToString(HttpStatus.NOT_FOUND.name())))
                .andExpect(jsonPath("$.message", stringContainsInOrder(exceptionMessage)));

        // Verify that the repository was called with the correct arguments
        verify(service, ONCE).update(id, request);
    }

}