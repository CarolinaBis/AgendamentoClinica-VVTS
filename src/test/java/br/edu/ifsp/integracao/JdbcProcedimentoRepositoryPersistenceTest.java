package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void deveSalvarNovoProcedimentoComIdGerado() {
        Procedimento procedimento = new Procedimento(null, "Consulta");

        Procedimento salvo = procedimentos.salvar(procedimento);

        assertNotNull(salvo.getId());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM procedimento", Integer.class));
        assertEquals("Consulta", jdbc.queryForObject(
                "SELECT nome FROM procedimento WHERE id = ?", String.class, salvo.getId()));
    }

    @Test
    void deveAtualizarProcedimentoExistente() {
        Procedimento original = procedimentos.salvar(new Procedimento(null, "Consulta"));
        Procedimento atualizado = new Procedimento(original.getId(), "Retorno");

        procedimentos.salvar(atualizado);

        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM procedimento", Integer.class));
        assertEquals("Retorno", jdbc.queryForObject(
                "SELECT nome FROM procedimento WHERE id = ?", String.class, original.getId()));
    }

    @Test
    void deveBuscarProcedimentoPorId() {
        jdbc.update("INSERT INTO procedimento (id, nome) VALUES (?, ?)", 60L, "Avaliação");

        Procedimento encontrado = procedimentos.buscarPorId(60L).orElseThrow();

        assertEquals(60L, encontrado.getId());
        assertEquals("Avaliação", encontrado.getNome());
    }

    @Test
    void deveRetornarVazioAoBuscarProcedimentoInexistente() {
        assertTrue(procedimentos.buscarPorId(Long.MAX_VALUE).isEmpty());
    }
}
