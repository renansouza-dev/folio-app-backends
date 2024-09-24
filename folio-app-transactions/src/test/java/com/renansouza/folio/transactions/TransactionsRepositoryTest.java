package com.renansouza.folio.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.renansouza.folio.transactions.TransactionsUtils.getEntities;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("Unit")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
})
class TransactionsRepositoryTest {

    @Autowired
    private TransactionsRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("DELETE FROM transactions;");
    }

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(repository).isNotNull();
    }

    @Test
    void list() {
        repository.saveAll(getEntities(2));

        assertThat(repository.findAllTransactions(Pageable.unpaged())).hasSize(2);
    }

    @Test
    void listAllByBroker() {
        var entities = getEntities(3);
        repository.saveAll(entities);

        var broker = entities.getFirst().getBroker();
        var filteredEntities = entities.stream().filter(entity -> broker.equals(entity.getBroker())).toList();

        var allByBroker = repository.findAllTransactionsByBroker(broker, Pageable.unpaged());

        assertThat(allByBroker)
                .isNotEmpty()
                .isInstanceOf(Page.class)
                .hasSize(filteredEntities.size());
    }

    @Test
    void listAllByAsset() {
        var entities = getEntities(3);
        repository.saveAll(entities);

        var asset = entities.getFirst().getAsset();
        var filteredEntities = entities.stream().filter(entity -> asset.equals(entity.getAsset())).toList();

        var allByAsset = repository.findAllTransactionsByAsset(asset, Pageable.unpaged());

        assertThat(allByAsset)
                .isNotEmpty()
                .isInstanceOf(Page.class)
                .hasSize(filteredEntities.size());
    }

    @Test
    void add() {
        var entity = repository.save(getEntities(1).getFirst());

        assertAll("ValidateEntityNotNull",
                () -> {
                    assertNotNull(entity);
                    assertAll("ValidateProperties",
                            () -> assertThat(entity.getId())
                                    .isExactlyInstanceOf(Long.class),
                            () -> assertThat(entity.getDate())
                                    .isNotNull()
                                    .isExactlyInstanceOf(LocalDate.class)
                                    .isEqualTo(LocalDate.now()),
                            () -> assertThat(entity.getAsset())
                                    .isNotNull()
                                    .isExactlyInstanceOf(String.class),
                            () -> assertThat(entity.getPrice())
                                    .isNotNull()
                                    .isExactlyInstanceOf(BigDecimal.class),
                            () -> assertThat(entity.getQuantity())
                                    .isNotNegative()
                                    .isExactlyInstanceOf(Integer.class),
                            () -> assertThat(entity.getFee())
                                    .isNotNegative()
                                    .isExactlyInstanceOf(BigDecimal.class),
                            () -> assertThat(entity.getBroker())
                                    .isNotNull()
                                    .isExactlyInstanceOf(String.class)
                    );
                }
        ) ;
    }

    @Test
    void delete() {
        var entity = repository.save(getEntities(1).getFirst());

        repository.deleteById(entity.getId());

        assertThat(repository.findAll()).isEmpty();
    }

}