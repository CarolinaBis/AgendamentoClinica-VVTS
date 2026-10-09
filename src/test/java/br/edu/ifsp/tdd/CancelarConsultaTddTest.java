package br.edu.ifsp.tdd;

import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US3 - Cancelar consulta")
class CancelarConsultaTddTest extends TesteBase {

    private int faltasVigentesDaMaria() {
        return pacientes.buscarPorId(maria.getId()).orElseThrow()
                .faltasVigentes(relogio.agora(), politica.faltas());
    }

    @Test
    @DisplayName("S3.1 - Cancelamento bem-sucedido")
    void s3_1_cancelamentoBemSucedido() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        Agendamento cancelado = agendamentoService.cancelar(ag.getId());

        assertEquals(StatusAgendamento.CANCELADO, cancelado.getStatus());
        assertEquals(StatusAgendamento.CANCELADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S3.2 - Cancelamento de agendamento já realizado")
    void s3_2_cancelamentoDeAgendamentoJaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.REALIZADO, consulta.getId());

        assertThrows(StatusInvalidoException.class, () -> agendamentoService.cancelar(ag.getId()));

        assertEquals(StatusAgendamento.REALIZADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S3.3 - Cancelamento tardio conta como falta")
    void s3_3_cancelamentoTardioContaComoFalta() {
        // agora = seg 09:00; consulta na terça 08:30 => 23h30 de antecedência (< 24h)
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(TERCA, 8, 30), List.of(consulta.getId()));

        Agendamento cancelado = agendamentoService.cancelar(ag.getId());

        assertEquals(StatusAgendamento.CANCELADO, cancelado.getStatus());
        assertEquals(1, faltasVigentesDaMaria());
    }

    @Test
    @DisplayName("S3.4 - Cancelamento com antecedência não gera penalidade")
    void s3_4_cancelamentoComAntecedenciaNaoGeraPenalidade() {
        // quarta 10:00 => 49h de antecedência (>= 24h)
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        agendamentoService.cancelar(ag.getId());

        assertEquals(0, faltasVigentesDaMaria());
    }
}
