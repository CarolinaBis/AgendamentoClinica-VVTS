package br.edu.ifsp.aplicacao;

import br.edu.ifsp.dominio.agendamento.Agendamento;

import java.time.LocalDate;
import java.util.List;

public record AgendaDoDia(LocalDate data, boolean expediente, List<Agendamento> agendamentos) {

    public AgendaDoDia {
        agendamentos = List.copyOf(agendamentos);
    }

    public String mensagem() {
        return expediente ? null : "Não há expediente nesse dia.";
    }
}
