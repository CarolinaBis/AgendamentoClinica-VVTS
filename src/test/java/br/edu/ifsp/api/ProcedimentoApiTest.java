package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.contains;

@Tag("ApiTest")
@Tag("IntegrationTest")
class ProcedimentoApiTest extends ApiTestBase {

    @Test
    @DisplayName("POST /procedimentos com nome válido devolve 201 com o procedimento criado")
    void cadastrarProcedimentoComNomeValidoDevolve201() {
        comToken()
                .body(Map.of("nome", "Consulta clínica"))
                .when()
                .post("/procedimentos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo("Consulta clínica"));
    }

    @Test
    @DisplayName("GET /procedimentos devolve 200 com os procedimentos cadastrados")
    void listarProcedimentosDevolve200() {
        criarProcedimento("Consulta clínica");
        criarProcedimento("Exame de sangue");

        comToken()
                .when()
                .get("/procedimentos")
                .then()
                .statusCode(200)
                .body("nome", contains("Consulta clínica", "Exame de sangue"));
    }
}