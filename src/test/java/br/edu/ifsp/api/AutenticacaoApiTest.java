package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
class AutenticacaoApiTest extends ApiTestBase {

    @Test
    @DisplayName("POST /register com dados válidos devolve 201 e o id do usuário")
    void registrarComDadosValidosDevolve201eIdUsuario() {
        semToken()
                .body(dadosDeRegistro(faker.internet().emailAddress()))
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue());
    }
}