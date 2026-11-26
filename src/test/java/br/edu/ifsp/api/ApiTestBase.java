package br.edu.ifsp.api;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite:target/api-test.db?foreign_keys=on")
public abstract class ApiTestBase {

    protected static final Faker faker = new Faker();
    protected static final String SENHA = "Senha@123";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    private String token;

    @BeforeEach
    void prepararApi() {
        RestAssured.port = porta;
        RestAssured.basePath = "/api/v1";
        limparBanco();
        token = registrarEAutenticar(faker.internet().emailAddress());
    }

    protected RequestSpecification semToken() {
        return RestAssured.given().contentType(ContentType.JSON);
    }

    protected RequestSpecification comToken() {
        return semToken().header("Authorization", "Bearer " + token);
    }

    protected Map<String, Object> dadosDeRegistro(String email) {
        return Map.of("name", faker.name().firstName(), "lastname", faker.name().lastName(),
                "email", email, "password", SENHA);
    }

    protected String registrarEAutenticar(String email) {
        semToken().body(dadosDeRegistro(email)).post("/register").then().statusCode(201);
        return semToken().body(Map.of("username", email, "password", SENHA))
                .post("/authenticate").then().statusCode(200)
                .extract().path("token");
    }

    protected long criarPaciente(String nome) {
        return comToken().body(Map.of("nome", nome)).post("/pacientes")
                .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    protected long criarProcedimento(String nome) {
        return comToken().body(Map.of("nome", nome)).post("/procedimentos")
                .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    protected LocalDateTime proximaSegundaAs(int hora) {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(hora, 0);
    }

    protected Map<String, Object> dadosDeAgendamento(long pacienteId, long profissionalId,
                                                     LocalDateTime inicio, long procedimentoId) {
        return Map.of("pacienteId", pacienteId, "profissionalId", profissionalId,
                "inicio", inicio.toString(), "fim", inicio.plusHours(1).toString(),
                "procedimentoIds", List.of(procedimentoId));
    }

    protected long agendar(long pacienteId, long profissionalId, LocalDateTime inicio, long procedimentoId) {
        return comToken().body(dadosDeAgendamento(pacienteId, profissionalId, inicio, procedimentoId))
                .post("/agendamentos")
                .then().statusCode(201).extract().jsonPath().getLong("id");
    }

    private void limparBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");
        jdbc.update("DELETE FROM app_user");
    }
}