package com.renansouza.folio.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.renansouza.folio.transactions.models.TransactionType;
import com.renansouza.folio.transactions.models.TransactionsEntity;
import com.renansouza.folio.transactions.models.TransactionsOperation;
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
                .set(field(TransactionsEntity::getPrice), BigDecimal.ONE)
                .set(field(TransactionsEntity::getQuantity), 1)
                .set(field(TransactionsEntity::getFee), BigDecimal.valueOf(0.01))
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

    public static String getFailureRequest() {
        return "{ \"type\": \"BUY\", \"asset\": \"ASSE11\", \"price\": 1139.74, \"quantity\": 4544, \"fee\": 9327.76, \"broker\": \"BROKER C\" }";
    }

    public static final String NOTIFICATION_FORMAT = "{\"account\":\"%s\",\"amount\":%s}";

    public static BigDecimal getAmount(TransactionsEntity entity, TransactionsOperation operation) {
        var total = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
        var totalWithFee = TransactionType.BUY.equals(entity.getType()) ? total.add(entity.getFee()) : total.subtract(entity.getFee());
        return shouldNegateTotal(entity.getType(), operation)
                ? totalWithFee.multiply(BigDecimal.valueOf(-1))
                : totalWithFee;
    }

    private static boolean shouldNegateTotal(TransactionType type, TransactionsOperation operation) {
        return (TransactionType.BUY.equals(type) && TransactionsOperation.SAVE.equals(operation)) ||
                (TransactionType.SELL.equals(type) && TransactionsOperation.DELETE.equals(operation));
    }

    private static final GeneratorSpecProvider<String> assetSpecProvider = gen -> gen.oneOf("ASSE1", "ASSE11");
    private static final GeneratorSpecProvider<String> brokerSpecProvider = gen -> gen.oneOf("BROKER A", "BROKER B", "BROKER C");

}
