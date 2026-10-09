package br.edu.ifsp.dominio.excecao;

public abstract class DominioException extends RuntimeException {
    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
