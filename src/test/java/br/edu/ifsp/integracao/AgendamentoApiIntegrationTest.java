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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @BeforeEach
    void configurarBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");

        pacienteId = inserirPaciente();
        procedimentoId = inserirProcedimento();
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
                .andExpect(jsonPath("$.status").value("UNPROCESSABLE_ENTITY"));
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

    private long inserirProcedimento() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO procedimento (nome) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "Consulta clinica");
            return statement;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
