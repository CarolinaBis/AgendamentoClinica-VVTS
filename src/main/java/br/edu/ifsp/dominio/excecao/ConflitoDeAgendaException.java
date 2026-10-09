package br.edu.ifsp.dominio.excecao;

public abstract class ConflitoDeAgendaException extends DominioException {
    protected ConflitoDeAgendaException(String mensagem) {
        super(mensagem);
    }
}
