package br.edu.ifsp.dominio.excecao;

public class HorarioNaoAtingidoException extends DominioException {
    public HorarioNaoAtingidoException() {
        super("O agendamento ainda não atingiu o horário previsto.");
    }
}
