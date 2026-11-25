package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/paciente-persistence-tests.db?foreign_keys=on")
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class JdbcPacienteRepositoryPersistenceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PacienteRepository pacientes;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM paciente");
    }

    @Test
    void deveSalvarNovoPacienteComIdGerado() {
        Paciente paciente = Paciente.novo("Maria da Silva");

        pacientes.salvar(paciente);

        assertNotNull(paciente.getId());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM paciente", Integer.class));
        assertEquals("Maria da Silva", jdbc.queryForObject(
                "SELECT nome FROM paciente WHERE id = ?", String.class, paciente.getId()));
    }
}
