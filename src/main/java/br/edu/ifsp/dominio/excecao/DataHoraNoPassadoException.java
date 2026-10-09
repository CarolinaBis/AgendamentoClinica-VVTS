package br.edu.ifsp.dominio.excecao;

public class DataHoraNoPassadoException extends DominioException {
    public DataHoraNoPassadoException() {
        super("Data/hora inválida: o horário solicitado já passou.");
    }

    public DataHoraNoPassadoException(String mensagem) {
        super(mensagem);
    }
}
