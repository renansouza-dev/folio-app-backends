package com.renansouza.folio.accounts;

import java.math.BigDecimal;
import java.util.UUID;

import com.renansouza.folio.accounts.models.AccountsEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountsRepository extends JpaRepository<AccountsEntity, UUID> {

    @Modifying
    @Transactional
    @Query("update AccountsEntity a set a.amount = (a.amount + :amount) where a.id = :id")
    void updateAmountById(@Param("id") UUID id, @Param("amount") BigDecimal amount);

}