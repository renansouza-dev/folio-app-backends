package com.renansouza.folio.accounts;

import java.math.BigDecimal;
import java.util.UUID;

import com.renansouza.folio.accounts.models.AccountsEntity;
import com.renansouza.folio.accounts.models.AccountsResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository extends JpaRepository<AccountsEntity, UUID> {

    @Modifying
    @Transactional
    @Query("update AccountsEntity a set a.amount = (a.amount + :amount) where a.id = :id")
    void updateAmountById(@Param("id") UUID id, @Param("amount") BigDecimal amount);

    @Query("""
            SELECT new com.renansouza.folio.accounts.models.AccountsResponse
            (a.id, a.broker, a.amount) FROM AccountsEntity a
            """)
    Page<AccountsResponse> findAllAccounts(Pageable pageable);

    Page<AccountsResponse> findByBroker(@Param("broker") String broker, Pageable pageable);

    boolean existsByBroker(@Param("broker") String broker);
}