package br.edu.ifsp.integracao;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Tag("ApiTest")
@Tag("IntegrationTest")
class AgendamentoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long pacienteId;
    private long procedimentoId;
    private long segundoProcedimentoId;

    @BeforeEach
    void configurarBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");

        pacienteId = inserirPaciente();
        procedimentoId = inserirProcedimento("Consulta clinica");
        segundoProcedimentoId = inserirProcedimento("Hemograma");
    }

    @Test
    void deveAgendarComDadosValidosERetornar422ParaDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T19:00:00",
                                  "fim": "2099-01-05T19:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("UNPROCESSABLE_ENTITY"))
                .andExpect(jsonPath("$.message").value(
                        "O horário solicitado está fora do horário de funcionamento da clínica."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage")
                        .value("br.edu.ifsp.dominio.excecao.ForaDoHorarioDeFuncionamentoException"));
    }

    @Test
    void deveBuscarAgendamentoERetornar404QuandoInexistente() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/v1/agendamentos/{id}", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        mockMvc.perform(get("/api/v1/agendamentos/{id}", agendamentoId + 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void deveRemarcarComPeriodoValidoERetornar422ParaPeriodoInvalido() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(put("/api/v1/agendamentos/{id}/periodo", agendamentoId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "inicio": "2099-01-05T11:00:00",
                                  "fim": "2099-01-05T11:30:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.inicio").value("2099-01-05T11:00:00"))
                .andExpect(jsonPath("$.fim").value("2099-01-05T11:30:00"))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        mockMvc.perform(put("/api/v1/agendamentos/{id}/periodo", agendamentoId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "inicio": "2020-01-05T11:00:00",
                                  "fim": "2020-01-05T11:30:00"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    void deveCancelarAgendamentoERetornar409AoCancelarNovamente() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/agendamentos/{id}/cancelamento", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.inicio").value("2099-01-05T09:00:00"))
                .andExpect(jsonPath("$.fim").value("2099-01-05T09:30:00"))
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        mockMvc.perform(post("/api/v1/agendamentos/{id}/cancelamento", agendamentoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void deveAdicionarProcedimentoERetornar409QuandoDuplicado() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/agendamentos/{id}/procedimentos", agendamentoId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"procedimentoId": %d}
                                """.formatted(segundoProcedimentoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos.length()").value(2))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId))
                .andExpect(jsonPath("$.procedimentos[1].procedimentoId").value(segundoProcedimentoId));

        mockMvc.perform(post("/api/v1/agendamentos/{id}/procedimentos", agendamentoId)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"procedimentoId": %d}
                                """.formatted(segundoProcedimentoId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void deveRemoverProcedimentoERetornar409AoRemoverUltimo() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d, %d]
                                }
                                """.formatted(pacienteId, procedimentoId, segundoProcedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(delete("/api/v1/agendamentos/{id}/procedimentos/{procedimentoId}",
                        agendamentoId, segundoProcedimentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos.length()").value(1))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        mockMvc.perform(delete("/api/v1/agendamentos/{id}/procedimentos/{procedimentoId}",
                        agendamentoId, procedimentoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    @Test
    void deveExecutarProcedimentoERetornar404QuandoNaoPertencerAoAgendamento() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/agendamentos/{id}/procedimentos/{procedimentoId}/execucao",
                        agendamentoId, procedimentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId))
                .andExpect(jsonPath("$.procedimentos[0].executado").value(true));

        mockMvc.perform(post("/api/v1/agendamentos/{id}/procedimentos/{procedimentoId}/execucao",
                        agendamentoId, segundoProcedimentoId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void deveConfirmarRealizacaoERetornar409EnquantoHaProcedimentosPendentes() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoId = ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();
        jdbc.update("UPDATE agendamento SET inicio = ?, fim = ? WHERE id = ?",
                "2020-01-05T09:00:00", "2020-01-05T09:30:00", agendamentoId);

        mockMvc.perform(post("/api/v1/agendamentos/{id}/realizacao", agendamentoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        jdbc.update("UPDATE agendamento_procedimento SET executado = 1 WHERE agendamento_id = ?",
                agendamentoId);

        mockMvc.perform(post("/api/v1/agendamentos/{id}/realizacao", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.profissionalId").value(10))
                .andExpect(jsonPath("$.inicio").value("2020-01-05T09:00:00"))
                .andExpect(jsonPath("$.fim").value("2020-01-05T09:30:00"))
                .andExpect(jsonPath("$.status").value("REALIZADO"))
                .andExpect(jsonPath("$.realizadoEm").isNotEmpty())
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId))
                .andExpect(jsonPath("$.procedimentos[0].executado").value(true));
    }

    @Test
    void deveRegistrarFaltaERetornar409SeHorarioAindaNaoFoiAtingido() throws Exception {
        MvcResult resultadoPassado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T09:00:00",
                                  "fim": "2099-01-05T09:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();

        long agendamentoPassadoId = ((Number) JsonPath.read(
                resultadoPassado.getResponse().getContentAsString(), "$.id")).longValue();
        jdbc.update("UPDATE agendamento SET inicio = ?, fim = ? WHERE id = ?",
                "2020-01-05T09:00:00", "2020-01-05T09:30:00", agendamentoPassadoId);

        mockMvc.perform(post("/api/v1/agendamentos/{id}/falta", agendamentoPassadoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoPassadoId))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.status").value("NAO_COMPARECEU"))
                .andExpect(jsonPath("$.procedimentos[0].procedimentoId").value(procedimentoId));

        MvcResult resultadoFuturo = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "pacienteId": %d,
                                  "profissionalId": 10,
                                  "inicio": "2099-01-05T10:00:00",
                                  "fim": "2099-01-05T10:30:00",
                                  "procedimentoIds": [%d]
                                }
                                """.formatted(pacienteId, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();
        long agendamentoFuturoId = ((Number) JsonPath.read(
                resultadoFuturo.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/v1/agendamentos/{id}/falta", agendamentoFuturoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"));
    }

    private long inserirPaciente() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO paciente (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "Maria da Silva");
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private long inserirProcedimento(String nome) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO procedimento (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, nome);
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
