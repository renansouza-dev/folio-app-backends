package com.renansouza.folio.transactions;

import java.time.LocalDate;
import java.util.List;

import com.renansouza.folio.transactions.models.TransactionsEntity;
import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import org.instancio.GeneratorSpecProvider;
import org.instancio.Instancio;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

public class TransactionsUtils {

    private TransactionsUtils() {}

    public static List<TransactionsEntity> getEntities(int size) {
        return Instancio.ofList(TransactionsEntity.class).size(size)
                .ignore(field(TransactionsEntity::getId))
                .generate(field(TransactionsEntity::getAsset), assetSpecProvider)
                .generate(field(TransactionsEntity::getBroker), brokerSpecProvider)
                .set(all(LocalDate.class), LocalDate.now())
                .create();
    }

    public static List<TransactionsRequest> getRequests(int size) {

        return Instancio.ofList(TransactionsRequest.class).size(size)
                .generate(field(TransactionsRequest::asset), assetSpecProvider)
                .generate(field(TransactionsRequest::broker), brokerSpecProvider)
                .set(all(LocalDate.class), LocalDate.now())
                .create();
    }

    public static List<TransactionsResponse> getResponses(int size) {

        return Instancio.ofList(TransactionsResponse.class).size(size)
                .generate(field(TransactionsResponse::asset), assetSpecProvider)
                .generate(field(TransactionsResponse::broker), brokerSpecProvider)
                .set(all(LocalDate.class), LocalDate.now())
                .create();
    }

    public static String getFailureResquest() {
        return "{ \"type\": \"BUY\", \"asset\": \"ASSE11\", \"price\": 1139.74, \"quantity\": 4544, \"fee\": 9327.76, \"broker\": \"BROKER C\" }";
    }

    private static final GeneratorSpecProvider<String> assetSpecProvider = gen -> gen.oneOf("ASSE1", "ASSE11");
    private static final GeneratorSpecProvider<String> brokerSpecProvider = gen -> gen.oneOf("BROKER A", "BROKER B", "BROKER C");

}
