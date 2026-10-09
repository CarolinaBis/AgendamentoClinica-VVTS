package br.edu.ifsp.controller.dto;

import br.edu.ifsp.dominio.procedimento.Procedimento;

public record ProcedimentoResponse(Long id, String nome) {

    public static ProcedimentoResponse de(Procedimento p) {
        return new ProcedimentoResponse(p.getId(), p.getNome());
    }
}
