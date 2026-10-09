package br.edu.ifsp.dominio.excecao;

public class ConflitoAgendaProfissionalException extends ConflitoDeAgendaException {
    public ConflitoAgendaProfissionalException() {
        super("Conflito de horário: o profissional já possui um agendamento ativo nesse período.");
    }
}
