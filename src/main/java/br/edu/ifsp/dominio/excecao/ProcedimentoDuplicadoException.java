package br.edu.ifsp.dominio.excecao;

public class ProcedimentoDuplicadoException extends DominioException {
    public ProcedimentoDuplicadoException() {
        super("O procedimento já está presente no agendamento.");
    }

    public ProcedimentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
