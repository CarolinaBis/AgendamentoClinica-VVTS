package br.edu.ifsp.dominio.excecao;

public class RecursoNaoEncontradoException extends DominioException {
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " não encontrado(a): id " + id + ".");
    }
}
