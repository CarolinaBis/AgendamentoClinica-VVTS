package br.edu.ifsp.integracao;

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

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
@Tag("ApiTest")
@Tag("IntegrationTest")
class ProfissionalApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long pacienteId;
    private long procedimentoId;

    @BeforeEach
    void configurarBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");

        pacienteId = inserirPaciente("Maria da Silva");
        procedimentoId = inserirProcedimento("Consulta clinica");
    }

    @Test
    void deveConsultarAgendaOrdenadaSemIncluirOutrosProfissionaisNemCancelados() throws Exception {
        LocalDate dia = LocalDate.of(2026, 11, 25);
        long profissionalId = 10;

        long agendamentoMaisTarde = inserirAgendamento(
                profissionalId, dia, "11:00", "11:30", "CONFIRMADO");
        long agendamentoMaisCedo = inserirAgendamento(
                profissionalId, dia, "09:00", "09:30", "CONFIRMADO");
        inserirAgendamento(20, dia, "09:30", "10:00", "CONFIRMADO");
        inserirAgendamento(profissionalId, dia, "10:00", "10:30", "CANCELADO");

        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", profissionalId)
                        .param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(dia.toString()))
                .andExpect(jsonPath("$.expediente").value(true))
                .andExpect(jsonPath("$.agendamentos.length()").value(2))
                .andExpect(jsonPath("$.agendamentos[0].id").value(agendamentoMaisCedo))
                .andExpect(jsonPath("$.agendamentos[1].id").value(agendamentoMaisTarde))
                .andExpect(jsonPath("$.agendamentos[0].profissionalId").value(profissionalId))
                .andExpect(jsonPath("$.agendamentos[1].profissionalId").value(profissionalId));
    }

    @Test
    void naoDeveIncluirAgendamentosDoMesmoProfissionalEmOutraData() throws Exception {
        LocalDate diaComAgendamento = LocalDate.of(2026, 11, 25);
        LocalDate diaConsultado = LocalDate.of(2026, 11, 26);
        inserirAgendamento(10, diaComAgendamento, "09:00", "09:30", "CONFIRMADO");

        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", 10)
                        .param("data", diaConsultado.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(diaConsultado.toString()))
                .andExpect(jsonPath("$.expediente").value(true))
                .andExpect(jsonPath("$.agendamentos").isArray())
                .andExpect(jsonPath("$.agendamentos.length()").value(0));
    }

    @Test
    void deveRetornarAgendaVaziaESemExpedienteNoFimDeSemana() throws Exception {
        LocalDate sabado = LocalDate.of(2026, 11, 28);
        inserirAgendamento(10, sabado, "09:00", "09:30", "CONFIRMADO");

        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", 10)
                        .param("data", sabado.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(sabado.toString()))
                .andExpect(jsonPath("$.expediente").value(false))
                .andExpect(jsonPath("$.mensagem").value("Não há expediente nesse dia."))
                .andExpect(jsonPath("$.agendamentos").isArray())
                .andExpect(jsonPath("$.agendamentos.length()").value(0));
    }

    @Test
    void deveUsarDataAtualQuandoParametroDataForOmitido() throws Exception {
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        boolean expedienteHoje = hoje.getDayOfWeek() != DayOfWeek.SATURDAY
                && hoje.getDayOfWeek() != DayOfWeek.SUNDAY;
        if (expedienteHoje) {
            inserirAgendamento(10, hoje, "09:00", "09:30", "CONFIRMADO");
        }

        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(hoje.toString()))
                .andExpect(jsonPath("$.expediente").value(expedienteHoje))
                .andExpect(jsonPath("$.agendamentos.length()").value(expedienteHoje ? 1 : 0));
    }

    @Test
    void deveRetornar400QuandoDataNaoSeguirFormatoIso() throws Exception {
        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", 10)
                        .param("data", "25/11/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Parse attempt failed for value [25/11/2026]"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage").value("java.lang.IllegalArgumentException"));
    }

    @Test
    void deveRetornar400QuandoIdDoProfissionalNaoForNumerico() throws Exception {
        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", "profissional-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("For input string: \"profissional-invalido\""))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.developerMessage").value("java.lang.NumberFormatException"));
    }

    @Test
    void deveRetornarAgendaVaziaParaIdNegativo() throws Exception {
        LocalDate dia = LocalDate.of(2026, 11, 26);

        mockMvc.perform(get("/api/v1/profissionais/{id}/agenda", -1)
                        .param("data", dia.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(dia.toString()))
                .andExpect(jsonPath("$.expediente").value(true))
                .andExpect(jsonPath("$.agendamentos.length()").value(0));
    }

    private long inserirPaciente(String nome) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO paciente (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, nome);
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

    private long inserirAgendamento(long profissionalId, LocalDate dia,
                                    String inicio, String fim, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO agendamento "
                            + "(paciente_id, profissional_id, inicio, fim, status, realizado_em) "
                            + "VALUES (?, ?, ?, ?, ?, NULL)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, pacienteId);
            statement.setLong(2, profissionalId);
            statement.setString(3, dia + "T" + inicio + ":00");
            statement.setString(4, dia + "T" + fim + ":00");
            statement.setString(5, status);
            return statement;
        }, keyHolder);

        long agendamentoId = keyHolder.getKey().longValue();
        jdbc.update("INSERT INTO agendamento_procedimento "
                        + "(agendamento_id, procedimento_id, ordem, executado) VALUES (?, ?, 0, 0)",
                agendamentoId, procedimentoId);
        return agendamentoId;
    }
}
