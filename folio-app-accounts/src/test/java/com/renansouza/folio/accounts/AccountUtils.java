package com.renansouza.folio.accounts;

import java.util.List;

import com.renansouza.folio.accounts.models.AccountsEntity;
import com.renansouza.folio.accounts.models.AccountsRequest;
import com.renansouza.folio.accounts.models.AccountsResponse;
import org.instancio.Instancio;

class AccountUtils {

    private AccountUtils() {}

    static List<AccountsEntity> getEntities(int size) {
        return Instancio.ofList(AccountsEntity.class).size(size).create();
    }

    static List<AccountsResponse> getResponses(int size) {
        return Instancio.ofList(AccountsResponse.class).size(size).create();
    }

    static List<AccountsRequest> getRequests(int size) {
        return Instancio.ofList(AccountsRequest.class).size(size).create();
    }

    static String getFailureRequest() { return "{ \"broker\":\"Broker A\" }"; }

}