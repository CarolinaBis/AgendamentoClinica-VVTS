package br.edu.ifsp.integracao;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.AgendamentoRepository;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.agendamento.ProcedimentoAgendado;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.UncategorizedSQLException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.datasource.url=jdbc:sqlite:target/persistence-tests.db?foreign_keys=on")
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class AgendamentoRepositoryPersistenceTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AgendamentoRepository agendamentos;

    @Autowired
    private PacienteRepository pacientes;

    @Autowired
    private ProcedimentoRepository procedimentos;

    @BeforeEach
    void limparBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");
    }

    @Test
    void deveImpedirAssociacaoDuplicadaDeProcedimentoNoMesmoAgendamento() {
        Paciente paciente = pacientes.salvar(Paciente.novo("Maria da Silva"));
        Procedimento procedimento = procedimentos.salvar(new Procedimento(null, "Consulta"));
        LocalDateTime inicio = LocalDateTime.of(2099, 1, 5, 9, 0);

        Agendamento agendamento = Agendamento.reconstituir(null, paciente.getId(), 10L,
                new PeriodoAtendimento(inicio, inicio.plusMinutes(30)),
                StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(procedimento.getId())), null);
        agendamentos.salvar(agendamento);

        UncategorizedSQLException erro = assertThrows(UncategorizedSQLException.class, () ->
                jdbc.update("""
                        INSERT INTO agendamento_procedimento
                        (agendamento_id, procedimento_id, ordem, executado)
                        VALUES (?, ?, ?, ?)
                        """, agendamento.getId(), procedimento.getId(), 1, 0));
        assertTrue(erro.getSQLException().getMessage().contains(
                "UNIQUE constraint failed: agendamento_procedimento.agendamento_id, "
                        + "agendamento_procedimento.procedimento_id"));

        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM agendamento_procedimento WHERE agendamento_id = ?",
                Integer.class, agendamento.getId()));
    }

    @Test
    void deveRecuperarAgendamentoComDadosEProcedimentosPersistidos() {
        Paciente paciente = pacientes.salvar(Paciente.novo("Ana Souza"));
        Procedimento primeiro = procedimentos.salvar(new Procedimento(null, "Avaliação"));
        Procedimento segundo = procedimentos.salvar(new Procedimento(null, "Retorno"));
        LocalDateTime inicio = LocalDateTime.of(2099, 2, 10, 9, 0);
        LocalDateTime realizadoEm = inicio.plusMinutes(45);

        Agendamento agendamento = Agendamento.reconstituir(null, paciente.getId(), 42L,
                new PeriodoAtendimento(inicio, inicio.plusMinutes(30)),
                StatusAgendamento.REALIZADO,
                List.of(
                        ProcedimentoAgendado.reconstituir(primeiro.getId(), true),
                        ProcedimentoAgendado.reconstituir(segundo.getId(), true)),
                realizadoEm);
        agendamentos.salvar(agendamento);

        Agendamento recuperado = agendamentos.buscarPorId(agendamento.getId()).orElseThrow();

        assertEquals(agendamento.getId(), recuperado.getId());
        assertEquals(paciente.getId(), recuperado.getPacienteId());
        assertEquals(42L, recuperado.getProfissionalId());
        assertEquals(inicio, recuperado.getPeriodo().inicio());
        assertEquals(inicio.plusMinutes(30), recuperado.getPeriodo().fim());
        assertEquals(StatusAgendamento.REALIZADO, recuperado.getStatus());
        assertEquals(realizadoEm, recuperado.getRealizadoEm());
        assertEquals(List.of(primeiro.getId(), segundo.getId()), recuperado.getProcedimentoIds());
        assertTrue(recuperado.getProcedimentos().stream().allMatch(
                ProcedimentoAgendado::isExecutado));
    }

    @Test
    void deveBuscarAgendamentosDoProfissionalEmOrdemCronologica() {
        Paciente paciente = pacientes.salvar(Paciente.novo("Carlos Lima"));
        Procedimento procedimento = procedimentos.salvar(new Procedimento(null, "Consulta"));
        LocalDateTime dia = LocalDateTime.of(2099, 3, 10, 9, 0);

        Agendamento maisTarde = Agendamento.reconstituir(null, paciente.getId(), 42L,
                new PeriodoAtendimento(dia.plusHours(2), dia.plusHours(2).plusMinutes(30)),
                StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(procedimento.getId())), null);
        Agendamento maisCedo = Agendamento.reconstituir(null, paciente.getId(), 42L,
                new PeriodoAtendimento(dia, dia.plusMinutes(30)),
                StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(procedimento.getId())), null);
        Agendamento outroProfissional = Agendamento.reconstituir(null, paciente.getId(), 99L,
                new PeriodoAtendimento(dia.plusHours(1), dia.plusHours(1).plusMinutes(30)),
                StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(procedimento.getId())), null);

        agendamentos.salvar(maisTarde);
        agendamentos.salvar(maisCedo);
        agendamentos.salvar(outroProfissional);

        assertEquals(List.of(maisCedo.getId(), maisTarde.getId()),
                agendamentos.buscarPorProfissional(42L).stream()
                        .map(Agendamento::getId)
                        .toList());
    }
}
