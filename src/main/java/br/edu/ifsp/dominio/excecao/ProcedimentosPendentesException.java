package br.edu.ifsp.dominio.excecao;

import java.util.List;

public class ProcedimentosPendentesException extends DominioException {

    private final List<Long> procedimentosPendentes;

    public ProcedimentosPendentesException(List<Long> procedimentosPendentes) {
        super("Confirme individualmente cada procedimento antes de concluir a consulta. Pendentes: "
                + procedimentosPendentes + ".");
        this.procedimentosPendentes = List.copyOf(procedimentosPendentes);
    }

    public List<Long> getProcedimentosPendentes() {
        return procedimentosPendentes;
    }
}
