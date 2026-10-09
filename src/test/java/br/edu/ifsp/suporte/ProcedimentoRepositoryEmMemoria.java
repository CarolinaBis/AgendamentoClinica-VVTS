package br.edu.ifsp.suporte;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;

import java.util.*;

public class ProcedimentoRepositoryEmMemoria implements ProcedimentoRepository {

    private final Map<Long, Procedimento> dados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Procedimento salvar(Procedimento procedimento) {
        Procedimento salvo = procedimento.getId() == null
                ? procedimento.comId(++sequencia)
                : procedimento;
        dados.put(salvo.getId(), salvo);
        return salvo;
    }

    @Override
    public Optional<Procedimento> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public boolean existePorId(Long id) {
        return dados.containsKey(id);
    }

    @Override
    public List<Procedimento> listar() {
        return new ArrayList<>(dados.values());
    }
}
