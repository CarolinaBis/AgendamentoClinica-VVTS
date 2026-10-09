package br.edu.ifsp.dominio.excecao;

public class ProcedimentoInvalidoException extends DominioException {
    public ProcedimentoInvalidoException() {
        super("Procedimento inválido: não existe no catálogo de procedimentos da clínica.");
    }
}
