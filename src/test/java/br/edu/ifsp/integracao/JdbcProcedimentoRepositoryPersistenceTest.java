package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/procedimento-persistence-tests.db?foreign_keys=on")
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class JdbcProcedimentoRepositoryPersistenceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ProcedimentoRepository procedimentos;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM procedimento");
    }

    @AfterEach
    void limparBancoAposTeste() {
        jdbc.update("DELETE FROM procedimento");
    }
}
