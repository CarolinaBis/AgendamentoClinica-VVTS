package br.edu.ifsp.dominio.excecao;

public class ConflitoAgendaPacienteException extends ConflitoDeAgendaException {
    public ConflitoAgendaPacienteException() {
        super("Conflito de horário: o paciente já possui outro agendamento ativo nesse período.");
    }
}
