package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.HorarioNaoAtingidoException;
import br.edu.ifsp.dominio.excecao.ProcedimentosPendentesException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;


@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US8 - Confirmar realização da consulta")
class ConfirmarRealizacaoTddTest extends TesteBase {

    private Agendamento agendamentoDaQuarta() {
        return agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId(), hemograma.getId()));
    }

    private void executarTodosOsProcedimentos(Agendamento ag) {
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), hemograma.getId());
    }

    @Test
    @DisplayName("S8.1 - Confirmação bem-sucedida")
    void s8_1_confirmacaoBemSucedida() {
        Agendamento ag = agendamentoDaQuarta();
        relogio.definir(QUARTA.atTime(10, 40));
        executarTodosOsProcedimentos(ag);

        Agendamento realizado = agendamentoService.confirmarRealizacao(ag.getId());

        assertEquals(StatusAgendamento.REALIZADO, realizado.getStatus());
        assertEquals(StatusAgendamento.REALIZADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S8.2 - Confirmação antes do horário previsto")
    void s8_2_confirmacaoAntesDoHorarioPrevisto() {
        Agendamento ag = agendamentoDaQuarta();
        executarTodosOsProcedimentos(ag);

        assertThrows(HorarioNaoAtingidoException.class,
                () -> agendamentoService.confirmarRealizacao(ag.getId()));

        assertEquals(StatusAgendamento.CONFIRMADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S8.3 - Confirmação de agendamento cancelado")
    void s8_3_confirmacaoDeAgendamentoCancelado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CANCELADO, consulta.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.confirmarRealizacao(ag.getId()));
    }

    @Test
    @DisplayName("S8.4 - Registro da data efetiva de atendimento")
    void s8_4_registroDaDataEfetivaDeAtendimento() {
        Agendamento ag = agendamentoDaQuarta();
        LocalDateTime momento = QUARTA.atTime(10, 45);
        relogio.definir(momento);
        executarTodosOsProcedimentos(ag);

        agendamentoService.confirmarRealizacao(ag.getId());

        Agendamento salvo = agendamentos.buscarPorId(ag.getId()).orElseThrow();
        assertEquals(StatusAgendamento.REALIZADO, salvo.getStatus());
        assertEquals(momento, salvo.getRealizadoEm());
        assertEquals(true, salvo.getProcedimentos().stream().allMatch(p -> p.isExecutado()));
    }

    @Test
    @DisplayName("S8.5 - Procedimentos pendentes de execução")
    void s8_5_procedimentosPendentesDeExecucao() {
        Agendamento ag = agendamentoDaQuarta();
        relogio.definir(QUARTA.atTime(10, 40));
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());

        ProcedimentosPendentesException ex = assertThrows(ProcedimentosPendentesException.class,
                () -> agendamentoService.confirmarRealizacao(ag.getId()));

        assertEquals(List.of(hemograma.getId()), ex.getProcedimentosPendentes());
        assertEquals(StatusAgendamento.CONFIRMADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }
}
