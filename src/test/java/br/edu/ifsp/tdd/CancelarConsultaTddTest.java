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
}
