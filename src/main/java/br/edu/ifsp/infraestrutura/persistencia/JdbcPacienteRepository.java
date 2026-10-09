package br.edu.ifsp.infraestrutura.persistencia;

import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPacienteRepository implements PacienteRepository {

    private final JdbcTemplate jdbc;

    public JdbcPacienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Paciente salvar(Paciente paciente) {
        if (paciente.getId() == null) {
            long id = ChavesGeradas.inserir(jdbc, "INSERT INTO paciente (nome) VALUES (?)", paciente.getNome());
            paciente.definirId(id);
        } else {
            jdbc.update("UPDATE paciente SET nome = ? WHERE id = ?", paciente.getNome(), paciente.getId());
        }
        jdbc.update("DELETE FROM falta WHERE paciente_id = ?", paciente.getId());
        for (Falta falta : paciente.getFaltas()) {
            jdbc.update("INSERT INTO falta (paciente_id, agendamento_id, data_referencia) VALUES (?, ?, ?)",
                    paciente.getId(), falta.agendamentoId(), falta.dataReferencia().toString());
        }
        return paciente;
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        List<String> nomes = jdbc.query("SELECT nome FROM paciente WHERE id = ?",
                (rs, n) -> rs.getString("nome"), id);
        if (nomes.isEmpty()) {
            return Optional.empty();
        }
        List<Falta> faltas = jdbc.query(
                "SELECT agendamento_id, data_referencia FROM falta WHERE paciente_id = ? ORDER BY id",
                (rs, n) -> new Falta(rs.getLong("agendamento_id"),
                        LocalDateTime.parse(rs.getString("data_referencia"))), id);
        return Optional.of(Paciente.reconstituir(id, nomes.get(0), faltas));
    }
}
