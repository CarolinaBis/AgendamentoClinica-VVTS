package br.edu.ifsp.dominio.excecao;

public class ForaDoHorarioDeFuncionamentoException extends DominioException {
    public ForaDoHorarioDeFuncionamentoException() {
        super("O horário solicitado está fora do horário de funcionamento da clínica.");
    }

    public ForaDoHorarioDeFuncionamentoException(String mensagem) {
        super(mensagem);
    }
}
