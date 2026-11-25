package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
class PacienteApiTest extends ApiTestBase {

    @Test
    @DisplayName("POST /pacientes com nome válido devolve 201 com o paciente criado")
    void cadastrarPacienteComNomeValidoDevolve201() {
        String nome = faker.name().fullName();

        comToken()
                .body(Map.of("nome", nome))
                .when()
                .post("/pacientes")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("nome", equalTo(nome));
    }
}