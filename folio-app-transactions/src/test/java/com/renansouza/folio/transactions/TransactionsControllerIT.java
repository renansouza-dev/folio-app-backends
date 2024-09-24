package com.renansouza.folio.transactions;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.renansouza.folio.transactions.TransactionsUtils.getEntities;
import static com.renansouza.folio.transactions.TransactionsUtils.getFailureResquest;
import static com.renansouza.folio.transactions.TransactionsUtils.getRequests;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@Tag("Integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionsControllerIT {

    private static final String PATH = "/v1/transactions";
    private static final int PAGE_SIZE = 20;
    private static final int TOTAL_PAGES = 1;

    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.3-alpine");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TransactionsRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repository.deleteAll();
    }

    @Test
    @DisplayName("should get zero transactions because database is empty.")
    void shouldGetZeroTransactions() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("should get zero transactions because the filter has no match.")
    void shouldGetZeroTransactionsByMismatchFilter() {
        repository.saveAll(getEntities(5));

        given()
                .param("broker", "BROKER")
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("should get all transactions because the is no set.")
    void shouldGetAllTransactions() {
        int elements = 2;
        repository.saveAll(getEntities(elements));

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", hasSize(elements))
                .body("page.size", is(PAGE_SIZE))
                .body("page.totalPages", is(TOTAL_PAGES))
                .body("page.totalElements", is(elements));
    }

    @Test
    @DisplayName("get all transactions filtering by broker.")
    void getTransactionsByBroker() {
        var transactions = repository.saveAll(getEntities(10));

        var filteredTransactions = transactions.stream().filter(t -> t.getBroker().equals(transactions.getLast().getBroker())).toList();
        var broker = filteredTransactions.getFirst().getBroker();

        given()
                .param("broker", broker)
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", hasSize(filteredTransactions.size()))
                .body("page.size", is(PAGE_SIZE))
                .body("page.totalPages", is(TOTAL_PAGES))
                .body("page.totalElements", is(filteredTransactions.size()));
    }

    @Test
    @DisplayName("get all transactions filtering by asset.")
    void getTransactionsByAsset() {
        var transactions = repository.saveAll(getEntities(10));

        var filteredTransactions = transactions.stream().filter(t -> t.getAsset().equals(transactions.getLast().getAsset())).toList();
        var asset = filteredTransactions.getFirst().getAsset();

        given()
                .param("asset", asset)
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", hasSize(filteredTransactions.size()))
                .body("page.size", is(PAGE_SIZE))
                .body("page.totalPages", is(TOTAL_PAGES))
                .body("page.totalElements", is(filteredTransactions.size()));
    }

    @Test
    @DisplayName("should add a new transactions to database.")
    void addTransaction() {
        given()
                .body(getRequests(1).getFirst())
                .contentType(ContentType.JSON)
                .when()
                .post(PATH)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DisplayName("should not add a new transactions to database because the payload is incorrect.")
    void failToAddTransaction() {
        given()
                .body(getFailureResquest())
                .contentType(ContentType.JSON)
                .when()
                .post(PATH)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("", Matchers.aMapWithSize(5),
                        "timestamp", Matchers.notNullValue(),
                        "status", Matchers.equalTo(HttpStatus.SC_BAD_REQUEST),
                        "error", Matchers.equalTo("BAD_REQUEST"),
                        "message", Matchers.containsString("date: Date cannot be null."),
                        "path", Matchers.equalTo(PATH));
    }

    @Test
    @DisplayName("should soft delete a transactions from database.")
    void shouldDeleteATransactions() {
        var id = repository.save(getEntities(TOTAL_PAGES).getFirst()).getId();

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .when()
                .delete(PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("should not soft delete a transactions from database because there is none.")
    void shouldNotDeleteATransactions() {
        var id = 1;

        given()
                .pathParam("id", id)
                .contentType(ContentType.JSON)
                .when()
                .delete(PATH + "/{id}")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("", Matchers.aMapWithSize(5),
                        "timestamp", Matchers.notNullValue(),
                        "status", Matchers.equalTo(HttpStatus.SC_NOT_FOUND),
                        "error", Matchers.equalTo("NOT_FOUND"),
                        "message", Matchers.equalTo(String.format("The provided id %s transaction was not found", id)),
                        "path", Matchers.equalTo(PATH + "/" + id));
    }

}