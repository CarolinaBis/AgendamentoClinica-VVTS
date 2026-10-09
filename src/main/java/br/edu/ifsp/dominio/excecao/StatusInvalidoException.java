package br.edu.ifsp.dominio.excecao;

import br.edu.ifsp.dominio.agendamento.StatusAgendamento;

public class StatusInvalidoException extends DominioException {

    private final StatusAgendamento status;

    public StatusInvalidoException(String acao, StatusAgendamento status) {
        super("Não é possível " + acao + " um agendamento " + status.getAdjetivo() + ".");
        this.status = status;
    }

    public StatusAgendamento getStatus() {
        return status;
    }
}
