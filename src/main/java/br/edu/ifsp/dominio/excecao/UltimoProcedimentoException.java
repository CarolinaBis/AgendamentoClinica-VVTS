package br.edu.ifsp.dominio.excecao;

public class UltimoProcedimentoException extends DominioException {
    public UltimoProcedimentoException() {
        super("Não é possível remover: o agendamento deve ter ao menos um procedimento.");
    }
}
