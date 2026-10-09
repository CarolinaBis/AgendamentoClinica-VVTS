package br.edu.ifsp.aplicacao;

import br.edu.ifsp.dominio.agendamento.*;
import br.edu.ifsp.dominio.excecao.IntervaloInvalidoException;
import br.edu.ifsp.dominio.excecao.PacienteBloqueadoException;
import br.edu.ifsp.dominio.excecao.ProcedimentoInvalidoException;
import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import br.edu.ifsp.dominio.politica.PoliticaClinica;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentos;
    private final PacienteRepository pacientes;
    private final ProcedimentoRepository procedimentos;
    private final DisponibilidadeService disponibilidade;
    private final PoliticaClinica politica;
    private final Clock relogio;

    public AgendamentoService(AgendamentoRepository agendamentos, PacienteRepository pacientes,
                              ProcedimentoRepository procedimentos, DisponibilidadeService disponibilidade,
                              PoliticaClinica politica, Clock relogio) {
        this.agendamentos = agendamentos;
        this.pacientes = pacientes;
        this.procedimentos = procedimentos;
        this.disponibilidade = disponibilidade;
        this.politica = politica;
        this.relogio = relogio;
    }

    @Transactional
    public Agendamento agendar(Long pacienteId, Long profissionalId, PeriodoAtendimento periodo,
                               List<Long> procedimentoIds) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        Agendamento novo = Agendamento.agendar(pacienteId, profissionalId, periodo,
                procedimentoIds, agora, politica.horario());

        Paciente paciente = buscarPaciente(pacienteId);
        exigirProcedimentosNoCatalogo(procedimentoIds);
        if (paciente.estaBloqueado(agora, politica.faltas())) {
            throw new PacienteBloqueadoException();
        }
        disponibilidade.garantirPacienteSemConflito(pacienteId, periodo, null);
        disponibilidade.garantirProfissionalDisponivel(profissionalId, periodo, null);

        return agendamentos.salvar(novo);
    }

    @Transactional
    public Agendamento remarcar(Long agendamentoId, PeriodoAtendimento novoPeriodo) {
        Agendamento agendamento = buscar(agendamentoId);
        agendamento.remarcar(novoPeriodo, LocalDateTime.now(relogio), politica.horario());

        disponibilidade.garantirPacienteSemConflito(agendamento.getPacienteId(), novoPeriodo, agendamento.getId());
        disponibilidade.garantirProfissionalDisponivel(agendamento.getProfissionalId(), novoPeriodo, agendamento.getId());

        return agendamentos.salvar(agendamento);
    }

    @Transactional
    public Agendamento cancelar(Long agendamentoId) {
        Agendamento agendamento = buscar(agendamentoId);
        boolean tardio = agendamento.cancelar(LocalDateTime.now(relogio), politica.prazoMinimoCancelamento());

        if (tardio) {
            Paciente paciente = buscarPaciente(agendamento.getPacienteId());
            paciente.registrarFalta(new Falta(agendamento.getId(), agendamento.getPeriodo().inicio()));
            pacientes.salvar(paciente);
        }
        return agendamentos.salvar(agendamento);
    }

    public Agendamento buscar(Long id) {
        return agendamentos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }

    public AgendaDoDia consultarAgenda(Long profissionalId, LocalDate data) {
        LocalDate dia = data != null ? data : LocalDate.now(relogio);
        if (!politica.horario().funcionaEm(dia)) {
            return new AgendaDoDia(dia, false, List.of());
        }
        List<Agendamento> doDia = agendamentos.buscarPorProfissional(profissionalId).stream()
                .filter(Agendamento::estaAtivo)
                .filter(a -> a.getPeriodo().data().equals(dia))
                .sorted(Comparator.comparing(a -> a.getPeriodo().inicio()))
                .toList();
        return new AgendaDoDia(dia, true, doDia);
    }

    public List<Agendamento> consultarHistorico(Long pacienteId, StatusAgendamento status,
                                                LocalDate de, LocalDate ate) {
        if (de != null && ate != null && ate.isBefore(de)) {
            throw new IntervaloInvalidoException();
        }
        buscarPaciente(pacienteId);
        return agendamentos.buscarPorPaciente(pacienteId).stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> de == null || !a.getPeriodo().data().isBefore(de))
                .filter(a -> ate == null || !a.getPeriodo().data().isAfter(ate))
                .sorted(Comparator.comparing(a -> a.getPeriodo().inicio()))
                .toList();
    }

    @Transactional
    public Agendamento adicionarProcedimento(Long agendamentoId, Long procedimentoId) {
        if (!procedimentos.existePorId(procedimentoId)) {
            throw new ProcedimentoInvalidoException();
        }
        Agendamento agendamento = buscar(agendamentoId);
        agendamento.adicionarProcedimento(procedimentoId);
        return agendamentos.salvar(agendamento);
    }

    @Transactional
    public Agendamento removerProcedimento(Long agendamentoId, Long procedimentoId) {
        Agendamento agendamento = buscar(agendamentoId);
        agendamento.removerProcedimento(procedimentoId);
        return agendamentos.salvar(agendamento);
    }

    @Transactional
    public Agendamento marcarProcedimentoComoExecutado(Long agendamentoId, Long procedimentoId) {
        Agendamento agendamento = buscar(agendamentoId);
        agendamento.marcarProcedimentoComoExecutado(procedimentoId);
        return agendamentos.salvar(agendamento);
    }

    @Transactional
    public Agendamento confirmarRealizacao(Long agendamentoId) {
        Agendamento agendamento = buscar(agendamentoId);
        agendamento.confirmarRealizacao(LocalDateTime.now(relogio));
        return agendamentos.salvar(agendamento);
    }

    @Transactional
    public Agendamento registrarFalta(Long agendamentoId) {
        Agendamento agendamento = buscar(agendamentoId);
        Falta falta = agendamento.registrarFalta(LocalDateTime.now(relogio));

        Paciente paciente = buscarPaciente(agendamento.getPacienteId());
        paciente.registrarFalta(falta);
        pacientes.salvar(paciente);
        return agendamentos.salvar(agendamento);
    }

    private void exigirProcedimentosNoCatalogo(List<Long> procedimentoIds) {
        if (procedimentoIds.stream().anyMatch(id -> !procedimentos.existePorId(id))) {
            throw new ProcedimentoInvalidoException();
        }
    }

    private Paciente buscarPaciente(Long id) {
        return pacientes.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }
}
