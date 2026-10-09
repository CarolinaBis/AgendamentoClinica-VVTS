package br.edu.ifsp.dominio.excecao;

public class PacienteBloqueadoException extends DominioException {
    public PacienteBloqueadoException() {
        super("Paciente bloqueado para novos agendamentos por excesso de faltas.");
    }

    public PacienteBloqueadoException(String mensagem) {
        super(mensagem);
    }
}
