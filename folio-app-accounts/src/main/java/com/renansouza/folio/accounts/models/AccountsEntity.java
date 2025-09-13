package com.renansouza.folio.accounts.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String broker;

    @Setter
    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal amount;

}