package br.edu.ifsp.dominio.paciente;

import java.util.Optional;

public interface PacienteRepository {
    Paciente salvar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);
}
