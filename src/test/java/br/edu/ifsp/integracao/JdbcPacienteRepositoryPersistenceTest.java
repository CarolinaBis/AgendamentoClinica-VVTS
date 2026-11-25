package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.paciente.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

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
}
