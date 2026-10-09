package br.edu.ifsp.suporte;

import br.edu.ifsp.aplicacao.AgendamentoService;
import br.edu.ifsp.aplicacao.PacienteService;
import br.edu.ifsp.dominio.agendamento.*;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.politica.PoliticaClinica;
import br.edu.ifsp.dominio.procedimento.Procedimento;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class TesteBase {

    protected static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 5, 9, 0);
    protected static final LocalDate SEGUNDA = LocalDate.of(2026, 10, 5);
    protected static final LocalDate TERCA = LocalDate.of(2026, 10, 6);
    protected static final LocalDate QUARTA = LocalDate.of(2026, 10, 7);
    protected static final LocalDate QUINTA = LocalDate.of(2026, 10, 8);
    protected static final LocalDate SEXTA = LocalDate.of(2026, 10, 9);
    protected static final LocalDate SABADO = LocalDate.of(2026, 10, 10);
    protected static final LocalDate DOMINGO = LocalDate.of(2026, 10, 11);

    protected static final Long PROFISSIONAL_A = 10L;
    protected static final Long PROFISSIONAL_B = 20L;

    protected RelogioMutavel relogio;
    protected PoliticaClinica politica;
    protected AgendamentoRepositoryEmMemoria agendamentos;
    protected PacienteRepositoryEmMemoria pacientes;
    protected ProcedimentoRepositoryEmMemoria procedimentos;

    protected AgendamentoService agendamentoService;
    protected PacienteService pacienteService;

    protected Paciente maria;
    protected Paciente joao;
    protected Procedimento consulta;
    protected Procedimento hemograma;
    protected Procedimento eletro;

    @BeforeEach
    void montarCenarioBase() {
        relogio = new RelogioMutavel(AGORA);
        politica = PoliticaClinica.padrao();
        agendamentos = new AgendamentoRepositoryEmMemoria();
        pacientes = new PacienteRepositoryEmMemoria();
        procedimentos = new ProcedimentoRepositoryEmMemoria();

        DisponibilidadeService disponibilidade = new DisponibilidadeService(agendamentos);
        agendamentoService = new AgendamentoService(agendamentos, pacientes, procedimentos,
                disponibilidade, politica, relogio);
        pacienteService = new PacienteService(pacientes, politica, relogio);

        maria = pacientes.salvar(Paciente.novo("Maria"));
        joao = pacientes.salvar(Paciente.novo("João"));
        consulta = procedimentos.salvar(new Procedimento(null, "Consulta clínica"));
        hemograma = procedimentos.salvar(new Procedimento(null, "Hemograma"));
        eletro = procedimentos.salvar(new Procedimento(null, "Eletrocardiograma"));
    }

    protected PeriodoAtendimento periodo(LocalDate dia, int hIni, int mIni, int hFim, int mFim) {
        return new PeriodoAtendimento(dia.atTime(hIni, mIni), dia.atTime(hFim, mFim));
    }

    protected PeriodoAtendimento meiaHora(LocalDate dia, int hora, int minuto) {
        LocalDateTime ini = dia.atTime(hora, minuto);
        return new PeriodoAtendimento(ini, ini.plusMinutes(30));
    }

    protected Agendamento persistirAgendamento(Paciente paciente, Long profissionalId,
                                               PeriodoAtendimento periodo, StatusAgendamento status,
                                               Long... procedimentoIds) {
        List<ProcedimentoAgendado> procs = new ArrayList<>();
        for (Long id : procedimentoIds) {
            procs.add(ProcedimentoAgendado.pendente(id));
        }
        Agendamento ag = Agendamento.reconstituir(null, paciente.getId(), profissionalId,
                periodo, status, procs, null);
        return agendamentos.salvar(ag);
    }

    protected Agendamento agendar(Paciente paciente, Long profissionalId, PeriodoAtendimento periodo,
                                  Procedimento... procs) {
        return agendamentoService.agendar(paciente.getId(), profissionalId, periodo,
                Arrays.stream(procs).map(Procedimento::getId).toList());
    }

    protected Agendamento agendamentoAtual(Agendamento ag) {
        return agendamentos.buscarPorId(ag.getId()).orElseThrow();
    }

    protected Paciente pacienteAtual(Paciente p) {
        return pacientes.buscarPorId(p.getId()).orElseThrow();
    }
}
