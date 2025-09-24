package com.renansouza.folio.transactions;

import com.renansouza.folio.transactions.models.TransactionType;
import com.renansouza.folio.transactions.models.TransactionsEntity;
import com.renansouza.folio.transactions.models.TransactionsOperation;
import com.renansouza.folio.transactions.models.TransactionsRequest;
import com.renansouza.folio.transactions.models.TransactionsResponse;

import static org.instancio.Select.all;
import static org.instancio.Select.field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.instancio.GeneratorSpecProvider;
import org.instancio.Instancio;

class TransactionsUtils {

  private static final GeneratorSpecProvider<String> assetSpecProvider = gen -> gen.oneOf("ASSE1",
      "ASSE11");

  private TransactionsUtils() {
  }

  static List<TransactionsEntity> getEntities(int size) {
    return Instancio.ofList(TransactionsEntity.class).size(size)
        .ignore(field(TransactionsEntity::getId))
        .generate(field(TransactionsEntity::getAsset), assetSpecProvider)
        .set(all(LocalDate.class), LocalDate.now())
        .set(field(TransactionsEntity::getPrice), BigDecimal.ONE)
        .set(field(TransactionsEntity::getQuantity), 1)
        .set(field(TransactionsEntity::getFee), BigDecimal.valueOf(0.01))
        .create();
  }

  static List<TransactionsRequest> getRequests(int size) {

    return Instancio.ofList(TransactionsRequest.class).size(size)
        .generate(field(TransactionsRequest::asset), assetSpecProvider)
        .set(all(LocalDate.class), LocalDate.now())
        .create();
  }

  static List<TransactionsResponse> getResponses(int size) {

    return Instancio.ofList(TransactionsResponse.class).size(size)
        .generate(field(TransactionsResponse::asset), assetSpecProvider)
        .set(all(LocalDate.class), LocalDate.now())
        .create();
  }

  static String getFailureRequest() {
    return "{ \"type\": \"BUY\", \"asset\": \"ASSE11\", \"price\": 1139.74, \"quantity\": 4544, \"fee\": 9327.76, \"broker\": \"9839a129-0b75-4eec-bc66-d7d98761950f\" }";
  }

  static BigDecimal getAmount(TransactionsEntity entity, TransactionsOperation operation) {
    var total = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
    var totalWithFee = TransactionType.BUY.equals(entity.getType()) ? total.add(entity.getFee())
        : total.subtract(entity.getFee());
    return shouldNegateTotal(entity.getType(), operation)
        ? totalWithFee.multiply(BigDecimal.valueOf(-1))
        : totalWithFee;
  }

  private static boolean shouldNegateTotal(TransactionType type, TransactionsOperation operation) {
    return (TransactionType.BUY.equals(type) && TransactionsOperation.SAVE.equals(operation)) ||
        (TransactionType.SELL.equals(type) && TransactionsOperation.DELETE.equals(operation));
  }

}