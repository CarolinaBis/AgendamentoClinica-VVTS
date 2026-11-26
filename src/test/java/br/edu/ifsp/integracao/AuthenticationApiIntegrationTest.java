package br.edu.ifsp.integracao;

import br.edu.ifsp.security.user.Role;
import br.edu.ifsp.security.user.User;
import br.edu.ifsp.security.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/auth-api-tests.db?foreign_keys=on")
@AutoConfigureMockMvc
@Tag("ApiTest")
@Tag("IntegrationTest")
class AuthenticationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository usuarios;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM app_user");
    }

    @AfterEach
    void limparBancoAposTeste() {
        jdbc.update("DELETE FROM app_user");
    }

    @Test
    void deveRegistrarUsuarioERetornarId() throws Exception {
        String senha = "Senha-forte-123!";

        MvcResult resultado = mockMvc.perform(post("/api/v1/register")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana",
                                  "lastname": "Souza",
                                  "email": "ana.souza@example.com",
                                  "password": "%s"
                                }
                                """.formatted(senha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        UUID id = UUID.fromString(JsonPath.read(
                resultado.getResponse().getContentAsString(), "$.id"));
        User usuario = usuarios.findByEmail("ana.souza@example.com").orElseThrow();

        assertEquals(id, usuario.getId());
        assertEquals("Ana", usuario.getName());
        assertEquals("Souza", usuario.getLastname());
        assertEquals(Role.USER, usuario.getRole());
        assertTrue(passwordEncoder.matches(senha, usuario.getPassword()));
    }
}
