package br.edu.ifsp.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
class ProcedimentoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");
    }

    @Test
    void deveCadastrarProcedimentoERetornar400ParaNomeEmBranco() throws Exception {
        mockMvc.perform(post("/api/v1/procedimentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "  Consulta especializada  "}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Consulta especializada"));

        mockMvc.perform(post("/api/v1/procedimentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void deveListarProcedimentosCadastradosEmOrdem() throws Exception {
        mockMvc.perform(post("/api/v1/procedimentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "Consulta"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/procedimentos")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"nome": "Hemograma"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/procedimentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].nome").value("Consulta"))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].nome").value("Hemograma"));
    }
}
