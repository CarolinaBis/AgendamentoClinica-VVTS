package br.edu.ifsp.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;



import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Tag("ApiTest")
@Tag("IntegrationTest")
class ProfissionalApiTest extends ApiTestBase {

    @Test
    @DisplayName("GET /profissionais/{id}/agenda num dia útil devolve 200 com os agendamentos do dia")
    void consultarAgendaEmDiaUtilDevolve200ComAgendamentos() {
        long pacienteId = criarPaciente(faker.name().fullName());
        long procedimentoId = criarProcedimento("Consulta clínica");
        LocalDateTime inicio = proximaSegundaAs(9);
        long agendamentoId = agendar(pacienteId, 10L, inicio, procedimentoId);

        comToken()
                .queryParam("data", inicio.toLocalDate().toString())
                .when()
                .get("/profissionais/{id}/agenda", 10)
                .then()
                .statusCode(200)
                .body("expediente", equalTo(true))
                .body("agendamentos", hasSize(1))
                .body("agendamentos[0].id", equalTo((int) agendamentoId));
    }
}