package br.edu.ifsp.dominio.agendamento;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {
    Agendamento salvar(Agendamento agendamento);

    Optional<Agendamento> buscarPorId(Long id);

    List<Agendamento> buscarPorProfissional(Long profissionalId);

    List<Agendamento> buscarPorPaciente(Long pacienteId);
}
