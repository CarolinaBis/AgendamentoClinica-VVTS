package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;

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
    void registrarComEmailJaCadastradoDevolve409() {
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

    @Test
    @DisplayName("POST /autenticar com credenciais válidas devolve 200 e o token")
    void autenticarComCredenciaisValidasDevolve200ComToken() {
        String email = faker.internet().emailAddress();
        semToken().body(dadosDeRegistro(email)).post("/register").then().statusCode(201);

        semToken()
                .body(Map.of("username", email, "password", SENHA))

                .when()
                .post("/authenticate")

                .then()
                .statusCode(200)
                .body("token", not(emptyOrNullString()));


    }

    @Test
    @DisplayName("POST /autenticar com senha incorreta devolve 401")
    void autenticarComSenhaIncorretaDevolve401() {
        String email = faker.internet().emailAddress();
        semToken().body(dadosDeRegistro(email)).post("/register").then().statusCode(201);

        semToken()
                .body(Map.of("username", email, "password", "senha-errada"))

                .when()
                .post("/authenticate")

                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Acessar um endpoint protegido sem token devolve 401")
    void acessarEndpointProtegidoSemTokenDevolve401() {
        semToken()
                .when()
                .get("/procedimentos")

                .then()
                .statusCode(401);
    }



}