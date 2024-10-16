package com.renansouza.folio.accounts.models;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;

@Entity
@Getter
@Generated
@SoftDelete
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class AccountsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String broker;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal amount;

    public AccountsEntity(String broker) {
        this.broker = broker;
    }

    public AccountsEntity(String broker, BigDecimal amount) {
        this.broker = broker;
        this.amount = amount;
    }

}