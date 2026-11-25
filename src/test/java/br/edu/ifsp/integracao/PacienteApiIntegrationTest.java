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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Tag("ApiTest")
@Tag("IntegrationTest")
class PacienteApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void configurarBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");
    }

    @Test
    void deveCadastrarPacienteERetornarSeusDados() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "Maria da Silva"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Maria da Silva"));
    }

    @Test
    void deveRetornar400AoCadastrarPacienteComNomeEmBranco() throws Exception {
        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("O nome do paciente é obrigatório."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage").value("java.lang.IllegalArgumentException"));
    }

    @Test
    void deveBuscarPacienteExistenteERetornarSeusDados() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");

        mockMvc.perform(get("/api/v1/pacientes/{id}", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pacienteId))
                .andExpect(jsonPath("$.nome").value("Maria da Silva"));
    }

    @Test
    void deveRetornar404AoBuscarPacienteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado(a): id 999999."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage")
                        .value("br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException"));
    }

    @Test
    void deveConsultarSituacaoDePacienteSemFaltas() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");

        mockMvc.perform(get("/api/v1/pacientes/{id}/faltas", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId))
                .andExpect(jsonPath("$.nome").value("Maria da Silva"))
                .andExpect(jsonPath("$.faltasVigentes").value(0))
                .andExpect(jsonPath("$.bloqueado").value(false));
    }

    private long cadastrarPaciente(String nome) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "%s"}
                                """.formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void deveRetornar404AoConsultarFaltasDePacienteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{id}/faltas", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado(a): id 999999."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage")
                        .value("br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException"));
    }

    @Test
    void deveFiltrarHistoricoDoPacientePorStatus() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");
        long procedimentoId = inserirProcedimento("Consulta clinica");

        criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-05");
        criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-06");
        long agendamentoCancelado = criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-07");

        mockMvc.perform(post("/api/v1/agendamentos/{id}/cancelamento", agendamentoCancelado))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/pacientes/{id}/agendamentos", pacienteId)
                        .param("status", "confirmado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.status == 'CONFIRMADO')]").exists())
                .andExpect(jsonPath("$[?(@.status == 'CANCELADO')]").isEmpty());
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

    private long criarAgendamentoNoDia(long pacienteId, long procedimentoId, String dia)
            throws Exception {
        return criarAgendamento(pacienteId, procedimentoId, dia + "T09:00:00", dia + "T09:30:00");
    }

    private long criarAgendamento(long pacienteId, long procedimentoId, String inicio, String fim)
            throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                            {
                              "pacienteId": %d,
                              "profissionalId": 10,
                              "inicio": "%s",
                              "fim": "%s",
                              "procedimentoIds": [%d]
                            }
                            """.formatted(pacienteId, inicio, fim, procedimentoId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id")).longValue();
    }

    @Test
    void deveFiltrarHistoricoDoPacientePorPeriodo() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");
        long procedimentoId = inserirProcedimento("Consulta clinica");

        long antesDoPeriodo = criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-05");
        long noInicioDoPeriodo = criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-06");
        long noFimDoPeriodo = criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-07");
        long depoisDoPeriodo = criarAgendamentoNoDia(pacienteId, procedimentoId, "2099-01-08");

        mockMvc.perform(get("/api/v1/pacientes/{id}/agendamentos", pacienteId)
                        .param("de", "2099-01-06")
                        .param("ate", "2099-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == " + noInicioDoPeriodo + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + noFimDoPeriodo + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + antesDoPeriodo + ")]").isEmpty())
                .andExpect(jsonPath("$[?(@.id == " + depoisDoPeriodo + ")]").isEmpty());
    }

    @Test
    void deveRetornar404AoConsultarHistoricoDePacienteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/pacientes/{id}/agendamentos", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Paciente não encontrado(a): id 999999."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage")
                        .value("br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException"));
    }

    @Test
    void deveRetornar400QuandoDataFinalDoHistoricoForAnteriorADataInicial() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");

        mockMvc.perform(get("/api/v1/pacientes/{id}/agendamentos", pacienteId)
                        .param("de", "2099-01-08")
                        .param("ate", "2099-01-06"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("Intervalo inválido: a data final é anterior à data inicial."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage")
                        .value("br.edu.ifsp.dominio.excecao.IntervaloInvalidoException"));
    }

    @Test
    void deveRetornar400QuandoStatusDoHistoricoForInvalido() throws Exception {
        long pacienteId = cadastrarPaciente("Maria da Silva");

        mockMvc.perform(get("/api/v1/pacientes/{id}/agendamentos", pacienteId)
                        .param("status", "desconhecido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(
                        "No enum constant br.edu.ifsp.dominio.agendamento.StatusAgendamento.DESCONHECIDO"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage").value("java.lang.IllegalArgumentException"));
    }
}
