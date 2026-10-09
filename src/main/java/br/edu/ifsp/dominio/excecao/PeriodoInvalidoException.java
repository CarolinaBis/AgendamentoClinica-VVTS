package br.edu.ifsp.dominio.excecao;

public class PeriodoInvalidoException extends DominioException {
    public PeriodoInvalidoException() {
        super("Período inválido.");
    }

    public PeriodoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
