package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

@Tag("ApiTest")
@Tag("IntegrationTest")
class AgendamentoApiTest extends ApiTestBase {

    @Test
    @DisplayName("POST /agendamentos com dados válidos devolve 201 com a consulta confirmada")
    void agendarComDadosValidosDevolve201() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");

        comToken()
                .body(dadosDeAgendamento(pacienteId, 10L, proximaSegundaAs(9), procedimentoId))
                .when()
                .post("/agendamentos")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("pacienteId", equalTo((int) pacienteId))
                .body("status", equalTo("CONFIRMADO"))
                .body("procedimentos", hasSize(1));
    }

    @Test
    @DisplayName("POST /agendamentos fora do horário de funcionamento devolve 422")
    void agendarForaDoHorarioDeFuncionamentoDevolve422() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");

        comToken()
                .body(dadosDeAgendamento(pacienteId, 10L, proximaSegundaAs(19), procedimentoId))
                .when()
                .post("/agendamentos")
                .then()
                .statusCode(422)
                .body("status", equalTo("UNPROCESSABLE_ENTITY"));
    }

    @Test
    @DisplayName("POST /agendamentos para um paciente inexistente devolve 404")
    void agendarParaPacienteInexistenteDevolve404() {
        long procedimentoId = criarProcedimento("Consulta clínica");

        comToken()
                .body(dadosDeAgendamento(99999L, 10L, proximaSegundaAs(9), procedimentoId))
                .when()
                .post("/agendamentos")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /agendamentos/{id} devolve 200 com o agendamento")
    void buscarAgendamentoDevolve200() {
        long agendamentoId = criarAgendamento();

        comToken()
                .when()
                .get("/agendamentos/{id}", agendamentoId)
                .then()
                .statusCode(200)
                .body("id", equalTo((int) agendamentoId))
                .body("status", equalTo("CONFIRMADO"));
    }

    private long criarAgendamento() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        return agendar(pacienteId, 10L, proximaSegundaAs(9), procedimentoId);
    }
}