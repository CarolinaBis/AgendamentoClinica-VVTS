package br.edu.ifsp.dominio.agendamento;

import br.edu.ifsp.dominio.excecao.ConflitoAgendaPacienteException;
import br.edu.ifsp.dominio.excecao.ConflitoAgendaProfissionalException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DisponibilidadeService {

    private final AgendamentoRepository agendamentos;

    public DisponibilidadeService(AgendamentoRepository agendamentos) {
        this.agendamentos = agendamentos;
    }

    public void garantirProfissionalDisponivel(Long profissionalId, PeriodoAtendimento periodo,
                                               Long agendamentoIgnoradoId) {
        if (existeSobreposicao(agendamentos.buscarPorProfissional(profissionalId), periodo, agendamentoIgnoradoId)) {
            throw new ConflitoAgendaProfissionalException();
        }
    }

    public void garantirPacienteSemConflito(Long pacienteId, PeriodoAtendimento periodo,
                                            Long agendamentoIgnoradoId) {
        if (existeSobreposicao(agendamentos.buscarPorPaciente(pacienteId), periodo, agendamentoIgnoradoId)) {
            throw new ConflitoAgendaPacienteException();
        }
    }

    private boolean existeSobreposicao(List<Agendamento> existentes, PeriodoAtendimento periodo,
                                       Long agendamentoIgnoradoId) {
        return existentes.stream()
                .filter(Agendamento::estaAtivo)
                .filter(a -> !a.getId().equals(agendamentoIgnoradoId))
                .anyMatch(a -> a.getPeriodo().sobrepoe(periodo));
    }
}
