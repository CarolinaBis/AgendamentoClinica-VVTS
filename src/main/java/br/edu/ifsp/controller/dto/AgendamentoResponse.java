package br.edu.ifsp.controller.dto;

import br.edu.ifsp.dominio.agendamento.Agendamento;

import java.time.LocalDateTime;
import java.util.List;

public record AgendamentoResponse(Long id, Long pacienteId, Long profissionalId, LocalDateTime inicio,
                                  LocalDateTime fim, String status, String statusRotulo,
                                  LocalDateTime realizadoEm, List<ProcedimentoAgendadoResponse> procedimentos) {

    public static AgendamentoResponse de(Agendamento a) {
        return new AgendamentoResponse(a.getId(), a.getPacienteId(), a.getProfissionalId(),
                a.getPeriodo().inicio(), a.getPeriodo().fim(), a.getStatus().name(), a.getStatus().getRotulo(),
                a.getRealizadoEm(),
                a.getProcedimentos().stream()
                        .map(p -> new ProcedimentoAgendadoResponse(p.getProcedimentoId(), p.isExecutado()))
                        .toList());
    }
}
