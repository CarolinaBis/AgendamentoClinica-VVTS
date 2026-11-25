package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import static org.hamcrest.Matchers.startsWith;

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

    @Test
    @DisplayName("PUT /agendamentos/{id}/periodo para um horário válido devolve 200 com o novo horário")
    void remarcarParaHorarioValidoDevolve200() {
        long agendamentoId = criarAgendamento();
        LocalDateTime novoInicio = proximaSegundaAs(14);

        comToken()
                .body(java.util.Map.of("inicio", novoInicio.toString(), "fim", novoInicio.plusHours(1).toString()))
                .when()
                .put("/agendamentos/{id}/periodo", agendamentoId)
                .then()
                .statusCode(200)
                .body("inicio", startsWith(novoInicio.toString()));
    }

    @Test
    @DisplayName("POST /agendamentos/{id}/cancelamento devolve 200 com o status cancelado")
    void cancelarAgendamentoDevolve200() {
        long agendamentoId = criarAgendamento();

        comToken()
                .when()
                .post("/agendamentos/{id}/cancelamento", agendamentoId)
                .then()
                .statusCode(200)
                .body("status", equalTo("CANCELADO"));
    }

    @Test
    @DisplayName("POST /agendamentos/{id}/procedimentos devolve 200 com o procedimento adicionado")
    void adicionarProcedimentoDevolve200() {
        long agendamentoId = criarAgendamento();
        long exameId = criarProcedimento("Exame de sangue");

        comToken()
                .body(java.util.Map.of("procedimentoId", exameId))
                .when()
                .post("/agendamentos/{id}/procedimentos", agendamentoId)
                .then()
                .statusCode(200)
                .body("procedimentos", hasSize(2))
                .body("procedimentos[1].procedimentoId", equalTo((int) exameId));
    }

    @Test
    @DisplayName("POST /agendamentos/{id}/procedimentos/{procedimentoId}/execucao devolve 200 com o procedimento executado")
    void executarProcedimentoDevolve200() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        long agendamentoId = agendar(pacienteId, 10L, proximaSegundaAs(9), procedimentoId);

        comToken()
                .when()
                .post("/agendamentos/{id}/procedimentos/{procedimentoId}/execucao", agendamentoId, procedimentoId)
                .then()
                .statusCode(200)
                .body("procedimentos[0].executado", equalTo(true));
    }

    @Test
    @DisplayName("DELETE /agendamentos/{id}/procedimentos/{procedimentoId} do único procedimento devolve 409")
    void removerUnicoProcedimentoDevolve409() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        long agendamentoId = agendar(pacienteId, 10L, proximaSegundaAs(9), procedimentoId);

        comToken()
                .when()
                .delete("/agendamentos/{id}/procedimentos/{procedimentoId}", agendamentoId, procedimentoId)
                .then()
                .statusCode(409)
                .body("status", equalTo("CONFLICT"));
    }

    @Test
    @DisplayName("POST /agendamentos/{id}/realizacao antes do horário da consulta devolve 409")
    void confirmarRealizacaoAntesDoHorarioDevolve409() {
        long agendamentoId = criarAgendamento();

        comToken()
                .when()
                .post("/agendamentos/{id}/realizacao", agendamentoId)
                .then()
                .statusCode(409)
                .body("status", equalTo("CONFLICT"));
    }

    private long criarAgendamento() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        return agendar(pacienteId, 10L, proximaSegundaAs(9), procedimentoId);
    }
}