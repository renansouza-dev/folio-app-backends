package com.renansouza.folio.accounts;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.renansouza.folio.accounts.models.AccountsEntity;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static com.renansouza.folio.accounts.AccountUtils.getEntities;
import static com.renansouza.folio.accounts.AccountUtils.getFailureRequest;
import static io.restassured.RestAssured.given;

@Tag("Integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.rabbitmq.listener.simple.auto-startup=false"})
class AccountsControllerIT {

    private static final String PATH = "/v1/accounts";
    private static final int PAGE_SIZE = 20;
    private static final int TOTAL_PAGES = 1;

    @LocalServerPort
    private Integer port;

    @Container
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
    AccountsRepository repository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repository.deleteAll();
    }

    @Test
    @DisplayName("should fail to add a new account because of bad input")
    void failToAddAccount() {
        given()
                .body(getFailureRequest())
                .contentType(ContentType.JSON)
                .when()
                .post(PATH)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("", Matchers.aMapWithSize(5),
                        "timestamp", Matchers.notNullValue(),
                        "status", Matchers.equalTo(HttpStatus.SC_BAD_REQUEST),
                        "error", Matchers.equalTo("BAD_REQUEST"),
                        "message", Matchers.containsString("amount: Amount cannot be null."),
                        "path", Matchers.equalTo(PATH));
    }

    @Test
    @DisplayName("should fail to add a new account because of conflict")
    void failToAddAccountConflict() {
        var entities = getEntities(1).getFirst();
        repository.save(entities);

        var expectedMessage = String.format("The provided broker %s already exists.", entities.getBroker());

        given()
                .body("{ \"broker\": \"" + entities.getBroker() + "\", \"amount\": 0.00}")
                .contentType(ContentType.JSON)
                .when()
                .post(PATH)
                .then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("", Matchers.aMapWithSize(5),
                        "timestamp", Matchers.notNullValue(),
                        "status", Matchers.equalTo(HttpStatus.SC_CONFLICT),
                        "error", Matchers.equalTo("CONFLICT"),
                        "message", Matchers.equalTo(expectedMessage),
                        "path", Matchers.equalTo(PATH));
    }

    @Test
    @DisplayName("should successfully add a new account")
    void addAccount() {
        given()
                .body("{ \"broker\": \"Broker A\", \"amount\": 0.00}")
                .contentType(ContentType.JSON)
                .when()
                .post(PATH)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DisplayName("should get no accounts")
    void getZeroAccounts() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("should get two accounts")
    void getTwoAccounts() {
        int size = 2;
        repository.saveAll(getEntities(size));

        given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("page.totalPages", Matchers.equalTo(TOTAL_PAGES),
                        "page.totalElements", Matchers.equalTo(size),
                        "page.size", Matchers.equalTo(PAGE_SIZE));
    }

    @Test
    @DisplayName("should get one account")
    void getOneAccount() {
        var entities = getEntities(2);
        var expectedAccount = entities.getLast();
        var savedEntitites = repository.saveAll(entities);

        given()
                .contentType(ContentType.JSON)
                .when()
                .param("broker", expectedAccount.getBroker())
                .get(PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content[0].id", Matchers.equalTo(savedEntitites.getLast().getId().toString()),
                        "content[0].broker", Matchers.equalTo(savedEntitites.getLast().getBroker()),
                        "page.totalPages", Matchers.equalTo(TOTAL_PAGES),
                        "page.totalElements", Matchers.equalTo(1),
                        "page.size", Matchers.equalTo(PAGE_SIZE));
    }

    private static Stream<Arguments> provideAccountsToUpdate() {
        return Stream.of(
                Arguments.of(getEntities(1).getFirst(), null, null),
                Arguments.of(getEntities(1).getFirst(), "Another Broker", null),
                Arguments.of(getEntities(1).getFirst(), null, BigDecimal.TEN)
                );
    }

    @ParameterizedTest
    @MethodSource("provideAccountsToUpdate")
    @DisplayName("should successfully update an account")
    void updateAccount(AccountsEntity entity, String broker, BigDecimal amount) {
        var savedEntity = repository.save(entity);

        broker = Objects.isNull(broker) ? savedEntity.getBroker() : broker;
        amount = Objects.isNull(amount) ? savedEntity.getAmount() : amount;

        given()
                .body(String.format("{ \"broker\": \"%s\", \"amount\": %s}", broker, amount))
                .contentType(ContentType.JSON)
                .when()
                .put(PATH + "/{id}", savedEntity.getId())
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    @DisplayName("should fail to update an account")
    void failToUpdateAccount() {
        given()
                .body("{ \"broker\": \"Broker A\", \"amount\": 0.00}")
                .contentType(ContentType.JSON)
                .when()
                .put(PATH + "/{id}", UUID.randomUUID())
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

}