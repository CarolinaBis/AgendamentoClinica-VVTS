package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

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

    @Test
    @DisplayName("POST /pacientes com nome em branco devolve 400")
    void cadastrarPacienteComNomeEmBrancoDevolve400(){
        comToken()
                .body(Map.of("nome", " " ))

                .when()
                .post("/pacientes")

                .then()
                .statusCode(400)
                .body("message", equalTo("O nome do paciente é obrigatório."));
    }

    @Test
    @DisplayName("GET /pacientes/{id} de um paciente existente devolve 200 com os dados")
    void buscarPacienteExistenteDevolve200(){
        String nome = faker.name().fullName();
        long id = criarPaciente(nome);

        comToken()
                .when()
                .get("/pacientes/{id}", id)

                .then()
                .statusCode(200)
                .body("id", equalTo((int) id))
                .body("nome", equalTo(nome));
    }

    @Test
    @DisplayName("GET /pacientes/{id} de um paciente inexistente devolve 404")
    void buscarPacienteInexistenteDevolve404() {
        comToken()
                .when()
                .get("/pacientes/{id}", 99999)

                .then()
                .statusCode(404)
                .body("status", equalTo("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /pacientes/{id}/faltas de um paciente novo devolve 200 sem faltas e não bloqueado")
    void consultarFaltasDePacienteNovoDevolve200() {
        String nome = faker.name().fullName();
        long id = criarPaciente(nome);

        comToken()
                .when()
                .get("/pacientes/{id}/faltas", id)
                .then()
                .statusCode(200)
                .body("pacienteId", equalTo((int) id))
                .body("nome", equalTo(nome))
                .body("faltasVigentes", equalTo(0))
                .body("bloqueado", equalTo(false));
    }

    @Test
    @DisplayName("GET /pacientes/{id}/agendamentos devolve 200 com o histórico do paciente")
    void consultarHistoricoDoPacienteDevolve200() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        long agendamentoId = agendar(pacienteId, 10L, proximaSegundaAs(9), procedimentoId);

        comToken()
                .when()
                .get("/pacientes/{id}/agendamentos", pacienteId)
                .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].id", equalTo((int) agendamentoId))
                .body("[0].pacienteId", equalTo((int) pacienteId));
    }
}