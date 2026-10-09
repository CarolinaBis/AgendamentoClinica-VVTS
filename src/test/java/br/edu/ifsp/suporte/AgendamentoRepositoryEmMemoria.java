package br.edu.ifsp.suporte;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.AgendamentoRepository;
import br.edu.ifsp.dominio.agendamento.ProcedimentoAgendado;

import java.util.*;

public class AgendamentoRepositoryEmMemoria implements AgendamentoRepository {

    private final Map<Long, Agendamento> dados = new LinkedHashMap<>();
    private long sequencia = 0;

    @Override
    public Agendamento salvar(Agendamento agendamento) {
        if (agendamento.getId() == null) {
            agendamento.definirId(++sequencia);
        }
        dados.put(agendamento.getId(), copiar(agendamento));
        return agendamento;
    }

    @Override
    public Optional<Agendamento> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id)).map(AgendamentoRepositoryEmMemoria::copiar);
    }

    @Override
    public List<Agendamento> buscarPorProfissional(Long profissionalId) {
        return dados.values().stream()
                .filter(a -> a.getProfissionalId().equals(profissionalId))
                .map(AgendamentoRepositoryEmMemoria::copiar)
                .toList();
    }

    @Override
    public List<Agendamento> buscarPorPaciente(Long pacienteId) {
        return dados.values().stream()
                .filter(a -> a.getPacienteId().equals(pacienteId))
                .map(AgendamentoRepositoryEmMemoria::copiar)
                .toList();
    }

    private static Agendamento copiar(Agendamento a) {
        List<ProcedimentoAgendado> procs = new ArrayList<>();
        for (ProcedimentoAgendado p : a.getProcedimentos()) {
            procs.add(ProcedimentoAgendado.reconstituir(p.getProcedimentoId(), p.isExecutado()));
        }
        return Agendamento.reconstituir(a.getId(), a.getPacienteId(), a.getProfissionalId(),
                a.getPeriodo(), a.getStatus(), procs, a.getRealizadoEm());
    }
}
