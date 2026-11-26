package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @AfterEach
    void limparBancoAposTeste() {
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

    @Test
    void deveSubstituirFaltasAoAtualizarPaciente() {
        Paciente original = Paciente.novo("Joao da Silva");
        original.registrarFalta(new Falta(10L, LocalDateTime.of(2026, 11, 23, 9, 0)));
        pacientes.salvar(original);

        List<Falta> faltasAtualizadas = List.of(
                new Falta(20L, LocalDateTime.of(2026, 11, 24, 9, 0)),
                new Falta(30L, LocalDateTime.of(2026, 11, 25, 9, 0)));
        Paciente atualizado = Paciente.reconstituir(
                original.getId(), "Joao da Silva Atualizado", faltasAtualizadas);

        pacientes.salvar(atualizado);

        assertEquals("Joao da Silva Atualizado", jdbc.queryForObject(
                "SELECT nome FROM paciente WHERE id = ?", String.class, original.getId()));
        List<Falta> faltasPersistidas = jdbc.query(
                "SELECT agendamento_id, data_referencia FROM falta WHERE paciente_id = ? ORDER BY id",
                (rs, rowNum) -> new Falta(rs.getLong("agendamento_id"),
                        LocalDateTime.parse(rs.getString("data_referencia"))),
                original.getId());
        assertEquals(faltasAtualizadas, faltasPersistidas);
    }

    @Test
    void deveRemoverTodasAsFaltasAoAtualizarPacienteSemFaltas() {
        Paciente original = Paciente.novo("Joao da Silva");
        original.registrarFalta(new Falta(10L, LocalDateTime.of(2026, 11, 23, 9, 0)));
        pacientes.salvar(original);
        Paciente atualizado = Paciente.reconstituir(original.getId(), "Joao da Silva", List.of());

        pacientes.salvar(atualizado);

        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM falta WHERE paciente_id = ?", Integer.class, original.getId()));
        assertTrue(pacientes.buscarPorId(original.getId()).orElseThrow().getFaltas().isEmpty());
    }

    @Test
    void deveRetornarVazioAoBuscarPacienteComIdNulo() {
        assertTrue(pacientes.buscarPorId(null).isEmpty());
    }

    @Test
    void deveRetornarVazioAoBuscarPacienteInexistente() {
        assertTrue(pacientes.buscarPorId(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void deveRecuperarPacientePersistidoSemFaltas() {
        Long pacienteId = 123L;
        jdbc.update("INSERT INTO paciente (id, nome) VALUES (?, ?)", pacienteId, "Ana Souza");

        Paciente recuperado = pacientes.buscarPorId(pacienteId).orElseThrow();

        assertEquals(pacienteId, recuperado.getId());
        assertEquals("Ana Souza", recuperado.getNome());
        assertTrue(recuperado.getFaltas().isEmpty());
    }

    @Test
    void deveRecuperarPacienteComFaltas() {
        Long pacienteId = 124L;
        jdbc.update("INSERT INTO paciente (id, nome) VALUES (?, ?)", pacienteId, "Beatriz Costa");
        jdbc.update("""
                INSERT INTO falta (paciente_id, agendamento_id, data_referencia)
                VALUES (?, ?, ?)
                """, pacienteId, 20L, "2026-11-24T09:00");
        jdbc.update("""
                INSERT INTO falta (paciente_id, agendamento_id, data_referencia)
                VALUES (?, ?, ?)
                """, pacienteId, 30L, "2026-11-25T09:00");

        Paciente recuperado = pacientes.buscarPorId(pacienteId).orElseThrow();

        assertEquals(pacienteId, recuperado.getId());
        assertEquals("Beatriz Costa", recuperado.getNome());
        assertEquals(List.of(
                new Falta(20L, LocalDateTime.of(2026, 11, 24, 9, 0)),
                new Falta(30L, LocalDateTime.of(2026, 11, 25, 9, 0))),
                recuperado.getFaltas());
    }
}
