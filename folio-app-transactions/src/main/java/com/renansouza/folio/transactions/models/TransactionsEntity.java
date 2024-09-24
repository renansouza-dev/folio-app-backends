package com.renansouza.folio.transactions.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.proxy.HibernateProxy;

@Entity
@Setter
@Getter
@Generated
@SoftDelete
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class TransactionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    @PastOrPresent
    @Column(nullable = false)
    private LocalDate date;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private TransactionType type;

    @NotNull
    @Size(min = 5, max = 6)
    @Column(nullable = false, length = 6)
    private String asset;

    @PositiveOrZero
    @Column(nullable = false, precision = 9)
    private BigDecimal price;

    @PositiveOrZero
    @Column(nullable = false, precision = 9)
    private int quantity;

    @PositiveOrZero
    @Column(nullable = false, precision = 9)
    private BigDecimal fee;

    @NotNull
    @Size(min = 5, max = 10)
    @Column(nullable = false, length = 10)
    private String broker;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionsEntity that)) return false;
        return quantity == that.quantity
                && Objects.equals(id, that.id)
                && Objects.equals(date, that.date)
                && type == that.type
                && Objects.equals(asset, that.asset)
                && Objects.equals(price, that.price)
                && Objects.equals(fee, that.fee)
                && Objects.equals(broker, that.broker);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

}