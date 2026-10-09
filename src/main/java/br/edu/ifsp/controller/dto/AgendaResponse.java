package br.edu.ifsp.controller.dto;

import br.edu.ifsp.aplicacao.AgendaDoDia;

import java.time.LocalDate;
import java.util.List;

public record AgendaResponse(LocalDate data, boolean expediente, String mensagem,
                             List<AgendamentoResponse> agendamentos) {

    public static AgendaResponse de(AgendaDoDia agenda) {
        return new AgendaResponse(agenda.data(), agenda.expediente(), agenda.mensagem(),
                agenda.agendamentos().stream().map(AgendamentoResponse::de).toList());
    }
}
