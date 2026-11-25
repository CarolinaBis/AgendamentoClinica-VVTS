package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;

@Tag("ApiTest")
@Tag("IntegrationTest")
class AutenticacaoApiTest extends ApiTestBase {

    @Test
    @DisplayName("POST /registro com dados válidos devolve 201 e o id do usuário")
    void registrarComDadosValidosDevolve201eIdUsuario() {
        semToken()
                .body(dadosDeRegistro(faker.internet().emailAddress()))
                .when()
                .post("/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("POST /registro com e-mail já cadastrado devolve 409")
    void registrarComEmailJaCadastradoDevolve409(){
        String email = faker.internet().emailAddress();
        semToken().body(dadosDeRegistro(email)).post("/register").then().statusCode(201);

        semToken()
                .body(dadosDeRegistro(email))

                .when()
                    .post("/register")

                .then()
                    .statusCode(409)
                    .body("message", containsString("already registered"));
    }

}