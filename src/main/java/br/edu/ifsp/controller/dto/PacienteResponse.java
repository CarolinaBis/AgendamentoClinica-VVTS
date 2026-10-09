package br.edu.ifsp.controller.dto;

import br.edu.ifsp.dominio.paciente.Paciente;

public record PacienteResponse(Long id, String nome) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome());
    }
}
