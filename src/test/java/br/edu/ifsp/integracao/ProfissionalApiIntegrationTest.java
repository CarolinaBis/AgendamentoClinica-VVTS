package br.edu.ifsp.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
import java.time.LocalDate;

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
