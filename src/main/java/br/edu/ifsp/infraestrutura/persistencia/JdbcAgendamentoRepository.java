package br.edu.ifsp.infraestrutura.persistencia;

import br.edu.ifsp.dominio.agendamento.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcAgendamentoRepository implements AgendamentoRepository {

    private static final String COLUNAS =
            "id, paciente_id, profissional_id, inicio, fim, status, realizado_em";

    private final JdbcTemplate jdbc;

    public JdbcAgendamentoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private record Linha(long id, long pacienteId, long profissionalId, PeriodoAtendimento periodo,
                         StatusAgendamento status, LocalDateTime realizadoEm) {
    }

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        String realizadoEm = agendamento.getRealizadoEm() == null ? null : agendamento.getRealizadoEm().toString();
        if (agendamento.getId() == null) {
            long id = ChavesGeradas.inserir(jdbc,
                    "INSERT INTO agendamento (paciente_id, profissional_id, inicio, fim, status, realizado_em) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    agendamento.getPacienteId(), agendamento.getProfissionalId(),
                    agendamento.getPeriodo().inicio().toString(), agendamento.getPeriodo().fim().toString(),
                    agendamento.getStatus().name(), realizadoEm);
            agendamento.definirId(id);
        } else {
            jdbc.update("UPDATE agendamento SET inicio = ?, fim = ?, status = ?, realizado_em = ? WHERE id = ?",
                    agendamento.getPeriodo().inicio().toString(), agendamento.getPeriodo().fim().toString(),
                    agendamento.getStatus().name(), realizadoEm, agendamento.getId());
        }
        jdbc.update("DELETE FROM agendamento_procedimento WHERE agendamento_id = ?", agendamento.getId());
        int ordem = 0;
        for (ProcedimentoAgendado p : agendamento.getProcedimentos()) {
            jdbc.update("INSERT INTO agendamento_procedimento (agendamento_id, procedimento_id, ordem, executado) "
                            + "VALUES (?, ?, ?, ?)",
                    agendamento.getId(), p.getProcedimentoId(), ordem++, p.isExecutado() ? 1 : 0);
        }
        return agendamento;
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return montar(jdbc.query("SELECT " + COLUNAS + " FROM agendamento WHERE id = ?",
                (rs, n) -> linha(rs), id)).stream().findFirst();
    }

    @Override
    public List<Agendamento> buscarPorProfissional(Long profissionalId) {
        if (profissionalId == null) {
            return List.of();
        }
        return montar(jdbc.query("SELECT " + COLUNAS + " FROM agendamento WHERE profissional_id = ? ORDER BY inicio",
                (rs, n) -> linha(rs), profissionalId));
    }

    @Override
    public List<Agendamento> buscarPorPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return List.of();
        }
        return montar(jdbc.query("SELECT " + COLUNAS + " FROM agendamento WHERE paciente_id = ? ORDER BY inicio",
                (rs, n) -> linha(rs), pacienteId));
    }

    private static Linha linha(java.sql.ResultSet rs) throws java.sql.SQLException {
        String realizado = rs.getString("realizado_em");
        return new Linha(rs.getLong("id"), rs.getLong("paciente_id"), rs.getLong("profissional_id"),
                new PeriodoAtendimento(LocalDateTime.parse(rs.getString("inicio")),
                        LocalDateTime.parse(rs.getString("fim"))),
                StatusAgendamento.valueOf(rs.getString("status")),
                realizado == null ? null : LocalDateTime.parse(realizado));
    }

    private List<Agendamento> montar(List<Linha> linhas) {
        return linhas.stream().map(l -> {
            List<ProcedimentoAgendado> procedimentos = jdbc.query(
                    "SELECT procedimento_id, executado FROM agendamento_procedimento "
                            + "WHERE agendamento_id = ? ORDER BY ordem",
                    (rs, n) -> ProcedimentoAgendado.reconstituir(rs.getLong("procedimento_id"),
                            rs.getInt("executado") == 1), l.id());
            return Agendamento.reconstituir(l.id(), l.pacienteId(), l.profissionalId(), l.periodo(),
                    l.status(), procedimentos, l.realizadoEm());
        }).toList();
    }
}
