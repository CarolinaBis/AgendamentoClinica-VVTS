package br.edu.ifsp.dominio.procedimento;

import java.util.List;
import java.util.Optional;

public interface ProcedimentoRepository {
    Procedimento salvar(Procedimento procedimento);

    Optional<Procedimento> buscarPorId(Long id);

    boolean existePorId(Long id);

    List<Procedimento> listar();
}
