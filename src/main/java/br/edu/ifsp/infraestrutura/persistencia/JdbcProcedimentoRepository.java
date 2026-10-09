package br.edu.ifsp.infraestrutura.persistencia;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcProcedimentoRepository implements ProcedimentoRepository {

    private final JdbcTemplate jdbc;

    public JdbcProcedimentoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Procedimento salvar(Procedimento procedimento) {
        if (procedimento.getId() == null) {
            long id = ChavesGeradas.inserir(jdbc, "INSERT INTO procedimento (nome) VALUES (?)", procedimento.getNome());
            return procedimento.comId(id);
        }
        jdbc.update("UPDATE procedimento SET nome = ? WHERE id = ?", procedimento.getNome(), procedimento.getId());
        return procedimento;
    }

    @Override
    public Optional<Procedimento> buscarPorId(Long id) {
        return jdbc.query("SELECT id, nome FROM procedimento WHERE id = ?",
                (rs, n) -> new Procedimento(rs.getLong("id"), rs.getString("nome")), id)
                .stream().findFirst();
    }

    @Override
    public boolean existePorId(Long id) {
        if (id == null) {
            return false;
        }
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM procedimento WHERE id = ?", Integer.class, id);
        return total != null && total > 0;
    }

    @Override
    public List<Procedimento> listar() {
        return jdbc.query("SELECT id, nome FROM procedimento ORDER BY id",
                (rs, n) -> new Procedimento(rs.getLong("id"), rs.getString("nome")));
    }
}
