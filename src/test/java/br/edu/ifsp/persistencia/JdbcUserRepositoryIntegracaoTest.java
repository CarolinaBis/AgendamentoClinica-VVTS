package br.edu.ifsp.persistencia;

import br.edu.ifsp.security.user.JdbcUserRepository;
import br.edu.ifsp.security.user.Role;
import br.edu.ifsp.security.user.User;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("Integration")
@DisplayName("Integração - JdbcUserRepository (SQLite)")
class JdbcUserRepositoryIntegracaoTest {

    private Path arquivo;
    private SingleConnectionDataSource dataSource;
    private JdbcUserRepository repositorio;

    @BeforeEach
    void abrirBanco() throws Exception {
        arquivo = Files.createTempFile("usuarios-teste-", ".db");
        dataSource = new SingleConnectionDataSource("jdbc:sqlite:" + arquivo + "?foreign_keys=on", true);
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        repositorio = new JdbcUserRepository(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void fecharBanco() throws Exception {
        dataSource.destroy();
        Files.deleteIfExists(arquivo);
    }

    private User usuario(String email) {
        return new User(UUID.randomUUID(), "John", "Snow", email, "$2a$hash-bcrypt", Role.USER);
    }

    @Test
    @DisplayName("Grava e busca por e-mail, preservando todos os campos")
    void gravaEBusca() {
        User novo = usuario("john@snow.com");
        repositorio.save(novo);

        User lido = repositorio.findByEmail("john@snow.com").orElseThrow();

        assertEquals(novo.getId(), lido.getId());
        assertEquals("John", lido.getName());
        assertEquals("Snow", lido.getLastname());
        assertEquals("$2a$hash-bcrypt", lido.getPassword());
        assertEquals("john@snow.com", lido.getUsername());
        assertEquals(Role.USER, lido.getRole());
        assertEquals("USER", lido.getAuthorities().iterator().next().getAuthority());
        assertTrue(lido.isEnabled() && lido.isAccountNonExpired() && lido.isAccountNonLocked()
                && lido.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("E-mail inexistente ou nulo: Optional vazio")
    void emailInexistenteOuNulo() {
        assertTrue(repositorio.findByEmail("ninguem@x.com").isEmpty());
        assertTrue(repositorio.findByEmail(null).isEmpty());
    }

    @Test
    @DisplayName("Salvar de novo o mesmo id atualiza (não duplica)")
    void salvarMesmoIdAtualiza() {
        User original = usuario("john@snow.com");
        repositorio.save(original);

        repositorio.save(new User(original.getId(), "Jon", "Targaryen", "john@snow.com", "novo-hash", Role.ADMIN));

        User lido = repositorio.findByEmail("john@snow.com").orElseThrow();
        assertEquals("Jon", lido.getName());
        assertEquals(Role.ADMIN, lido.getRole());
        assertEquals("novo-hash", lido.getPassword());
    }

    @Test
    @DisplayName("O banco impede dois usuários com o mesmo e-mail")
    void emailUnico() {
        repositorio.save(usuario("john@snow.com"));

        assertThrows(DataAccessException.class, () -> repositorio.save(usuario("john@snow.com")));
    }

    @Test
    @DisplayName("Campos obrigatórios nulos são rejeitados e toString não vaza a senha")
    void invariantes() {
        assertThrows(NullPointerException.class,
                () -> new User(UUID.randomUUID(), null, "Snow", "a@b.com", "x", Role.USER));
        assertThrows(NullPointerException.class,
                () -> new User(UUID.randomUUID(), "John", "Snow", "a@b.com", "x", null));
        assertFalse(usuario("john@snow.com").toString().contains("bcrypt"));
    }
}
