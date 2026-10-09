package br.edu.ifsp.controller.dto;

import br.edu.ifsp.aplicacao.SituacaoDoPaciente;

public record SituacaoPacienteResponse(Long pacienteId, String nome, int faltasVigentes, boolean bloqueado) {

    public static SituacaoPacienteResponse de(SituacaoDoPaciente s) {
        return new SituacaoPacienteResponse(s.pacienteId(), s.nome(), s.faltasVigentes(), s.bloqueado());
    }
}
