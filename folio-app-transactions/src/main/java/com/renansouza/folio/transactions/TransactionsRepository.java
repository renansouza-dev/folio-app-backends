package com.renansouza.folio.transactions;

import com.renansouza.folio.transactions.models.TransactionsEntity;
import com.renansouza.folio.transactions.models.TransactionsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionsRepository extends JpaRepository<TransactionsEntity, Long> {

    String QUERY = """
            SELECT new com.renansouza.folio.transactions.models.TransactionsResponse
            (t.id, t.date, t.type, t.asset, t.price, t.quantity, t.fee, t.broker)
            FROM TransactionsEntity t
            """;

    @Query(QUERY)
    Page<TransactionsResponse> findAllTransactions(Pageable pageable);

    @Query(QUERY + " WHERE t.broker = :broker")
    Page<TransactionsResponse> findAllTransactionsByBroker(@Param("broker") String broker, Pageable pageable);

    @Query(QUERY + " WHERE t.asset = :asset")
    Page<TransactionsResponse> findAllTransactionsByAsset(@Param("asset")String asset, Pageable pageable);

}