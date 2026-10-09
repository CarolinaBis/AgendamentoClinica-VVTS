package br.edu.ifsp.dominio.excecao;

public class ProcedimentoObrigatorioException extends DominioException {
    public ProcedimentoObrigatorioException() {
        super("O agendamento exige ao menos um procedimento.");
    }

    public ProcedimentoObrigatorioException(String mensagem) {
        super(mensagem);
    }
}
