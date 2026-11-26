package br.edu.ifsp.integracao;

import br.edu.ifsp.security.user.Role;
import br.edu.ifsp.security.user.User;
import br.edu.ifsp.security.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/user-persistence-tests.db")
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class JdbcUserRepositoryPersistenceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository usuarios;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM app_user");
    }

    @AfterEach
    void limparBancoAposTeste() {
        jdbc.update("DELETE FROM app_user");
    }

    @Test
    void deveSalvarERecuperarUsuarioPorEmail() {
        UUID id = UUID.randomUUID();
        User usuario = new User(id, "Ana", "Souza", "ana@example.com", "senha-hash", Role.USER);

        usuarios.save(usuario);

        User recuperado = usuarios.findByEmail("ana@example.com").orElseThrow();

        assertEquals(id, recuperado.getId());
        assertEquals("Ana", recuperado.getName());
        assertEquals("Souza", recuperado.getLastname());
        assertEquals("ana@example.com", recuperado.getEmail());
        assertEquals("senha-hash", recuperado.getPassword());
        assertEquals(Role.USER, recuperado.getRole());
    }
}
