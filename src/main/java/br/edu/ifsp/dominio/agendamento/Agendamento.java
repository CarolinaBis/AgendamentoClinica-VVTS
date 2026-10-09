package br.edu.ifsp.dominio.agendamento;

import br.edu.ifsp.dominio.excecao.*;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.politica.HorarioFuncionamento;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Agendamento {

    private Long id;
    private final Long pacienteId;
    private final Long profissionalId;
    private PeriodoAtendimento periodo;
    private StatusAgendamento status;
    private final List<ProcedimentoAgendado> procedimentos;
    private LocalDateTime realizadoEm;

    private Agendamento(Long id, Long pacienteId, Long profissionalId, PeriodoAtendimento periodo,
                        StatusAgendamento status, List<ProcedimentoAgendado> procedimentos,
                        LocalDateTime realizadoEm) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.profissionalId = profissionalId;
        this.periodo = periodo;
        this.status = status;
        this.procedimentos = new ArrayList<>(procedimentos);
        this.realizadoEm = realizadoEm;
    }

    public static Agendamento agendar(Long pacienteId, Long profissionalId, PeriodoAtendimento periodo,
                                      List<Long> procedimentoIds, LocalDateTime agora,
                                      HorarioFuncionamento horario) {
        if (profissionalId == null) {
            throw new DadosInvalidosException("O profissional é obrigatório.");
        }
        if (procedimentoIds == null || procedimentoIds.isEmpty()) {
            throw new ProcedimentoObrigatorioException();
        }
        if (procedimentoIds.stream().anyMatch(Objects::isNull)) {
            throw new ProcedimentoInvalidoException();
        }
        Set<Long> unicos = new HashSet<>(procedimentoIds);
        if (unicos.size() != procedimentoIds.size()) {
            throw new ProcedimentoDuplicadoException();
        }
        validarPeriodo(periodo, agora, horario);

        List<ProcedimentoAgendado> procs = new ArrayList<>();
        for (Long procId : procedimentoIds) {
            procs.add(ProcedimentoAgendado.pendente(procId));
        }
        return new Agendamento(null, pacienteId, profissionalId, periodo,
                StatusAgendamento.CONFIRMADO, procs, null);
    }

    public static Agendamento reconstituir(Long id, Long pacienteId, Long profissionalId,
                                           PeriodoAtendimento periodo, StatusAgendamento status,
                                           List<ProcedimentoAgendado> procedimentos,
                                           LocalDateTime realizadoEm) {
        return new Agendamento(id, pacienteId, profissionalId, periodo, status, procedimentos, realizadoEm);
    }

    private static void validarPeriodo(PeriodoAtendimento periodo, LocalDateTime agora,
                                       HorarioFuncionamento horario) {
        if (periodo.inicio().isBefore(agora)) {
            throw new DataHoraNoPassadoException();
        }
        if (!horario.comporta(periodo)) {
            throw new ForaDoHorarioDeFuncionamentoException();
        }
    }

    public void remarcar(PeriodoAtendimento novoPeriodo, LocalDateTime agora, HorarioFuncionamento horario) {
        exigirConfirmado("remarcar");
        validarPeriodo(novoPeriodo, agora, horario);
        this.periodo = novoPeriodo;
    }

    public boolean cancelar(LocalDateTime agora, Duration prazoMinimo) {
        exigirConfirmado("cancelar");
        boolean tardio = Duration.between(agora, periodo.inicio()).compareTo(prazoMinimo) < 0;
        this.status = StatusAgendamento.CANCELADO;
        return tardio;
    }

    private void exigirConfirmado(String acao) {
        if (status != StatusAgendamento.CONFIRMADO) {
            throw new StatusInvalidoException(acao, status);
        }
    }

    public void adicionarProcedimento(Long procedimentoId) {
        exigirConfirmado("adicionar procedimento a");
        if (contem(procedimentoId)) {
            throw new ProcedimentoDuplicadoException();
        }
        procedimentos.add(ProcedimentoAgendado.pendente(procedimentoId));
    }

    public void removerProcedimento(Long procedimentoId) {
        exigirConfirmado("remover procedimento de");
        if (!contem(procedimentoId)) {
            throw new ProcedimentoNaoEncontradoException();
        }
        if (procedimentos.size() == 1) {
            throw new UltimoProcedimentoException();
        }
        procedimentos.removeIf(p -> p.getProcedimentoId().equals(procedimentoId));
    }

    private boolean contem(Long procedimentoId) {
        return procedimentos.stream().anyMatch(p -> p.getProcedimentoId().equals(procedimentoId));
    }
    public void marcarProcedimentoComoExecutado(Long procedimentoId) {
        exigirConfirmado("registrar a execução de procedimento em");
        procedimentos.stream()
                .filter(p -> p.getProcedimentoId().equals(procedimentoId))
                .findFirst()
                .orElseThrow(ProcedimentoNaoEncontradoException::new)
                .marcarExecutado();
    }

    public void confirmarRealizacao(LocalDateTime agora) {
        exigirConfirmado("confirmar a realização de");
        exigirHorarioAtingido(agora);
        List<Long> pendentes = procedimentos.stream()
                .filter(p -> !p.isExecutado())
                .map(ProcedimentoAgendado::getProcedimentoId)
                .toList();
        if (!pendentes.isEmpty()) {
            throw new ProcedimentosPendentesException(pendentes);
        }
        this.status = StatusAgendamento.REALIZADO;
        this.realizadoEm = agora;
    }

    public Falta registrarFalta(LocalDateTime agora) {
        exigirConfirmado("registrar falta em");
        exigirHorarioAtingido(agora);
        this.status = StatusAgendamento.NAO_COMPARECEU;
        return new Falta(id, periodo.inicio());
    }

    private void exigirHorarioAtingido(LocalDateTime agora) {
        if (agora.isBefore(periodo.inicio())) {
            throw new HorarioNaoAtingidoException();
        }
    }

    public boolean estaAtivo() {
        return status != StatusAgendamento.CANCELADO;
    }

    public void definirId(Long novoId) {
        if (this.id != null) {
            throw new IllegalStateException("O agendamento já possui id.");
        }
        this.id = novoId;
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getProfissionalId() {
        return profissionalId;
    }

    public PeriodoAtendimento getPeriodo() {
        return periodo;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public List<ProcedimentoAgendado> getProcedimentos() {
        return Collections.unmodifiableList(procedimentos);
    }

    public List<Long> getProcedimentoIds() {
        return procedimentos.stream().map(ProcedimentoAgendado::getProcedimentoId).toList();
    }
}
