package br.edu.ifsp.aplicacao;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcedimentoService {

    private final ProcedimentoRepository procedimentos;

    public ProcedimentoService(ProcedimentoRepository procedimentos) {
        this.procedimentos = procedimentos;
    }

    @Transactional
    public Procedimento cadastrar(String nome) {
        return procedimentos.salvar(new Procedimento(null, nome));
    }

    public List<Procedimento> listar() {
        return procedimentos.listar();
    }
}
