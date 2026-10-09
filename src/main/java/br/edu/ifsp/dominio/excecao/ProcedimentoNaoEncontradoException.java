package br.edu.ifsp.dominio.excecao;

public class ProcedimentoNaoEncontradoException extends DominioException {
    public ProcedimentoNaoEncontradoException() {
        super("Procedimento não encontrado: ele não pertence a este agendamento.");
    }
}
