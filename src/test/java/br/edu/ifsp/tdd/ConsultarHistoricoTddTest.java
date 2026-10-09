package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.IntervaloInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US5 - Consultar histórico de agendamentos do paciente")
class ConsultarHistoricoTddTest extends TesteBase {

    @Test
    @DisplayName("S5.1 - Histórico com agendamentos")
    void s5_1_historicoComAgendamentos() {
        Agendamento a1 = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
        Agendamento a2 = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), List.of(consulta.getId()));
        agendamentoService.cancelar(a2.getId());

        List<Agendamento> historico = agendamentoService.consultarHistorico(maria.getId(), null, null, null);

        assertEquals(2, historico.size());
        assertEquals(Set.of(StatusAgendamento.CONFIRMADO, StatusAgendamento.CANCELADO),
                historico.stream().map(Agendamento::getStatus).collect(Collectors.toSet()));
        assertTrue(historico.stream().anyMatch(a -> a.getId().equals(a1.getId())));
    }

    @Test
    @DisplayName("S5.2 - Histórico vazio")
    void s5_2_historicoVazio() {
        assertTrue(agendamentoService.consultarHistorico(joao.getId(), null, null, null).isEmpty());
    }

    @Test
    @DisplayName("S5.3 - Filtro por status")
    void s5_3_filtroPorStatus() {
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
        Agendamento a2 = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), List.of(consulta.getId()));
        agendamentoService.cancelar(a2.getId());

        List<Agendamento> cancelados = agendamentoService
                .consultarHistorico(maria.getId(), StatusAgendamento.CANCELADO, null, null);

        assertEquals(1, cancelados.size());
        assertEquals(a2.getId(), cancelados.get(0).getId());
    }

    @Test
    @DisplayName("S5.4 - Filtro por período")
    void s5_4_filtroPorPeriodo() {
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
        Agendamento quinta = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), List.of(consulta.getId()));
        Agendamento sexta = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(SEXTA, 10, 0), List.of(consulta.getId()));

        List<Agendamento> noPeriodo = agendamentoService
                .consultarHistorico(maria.getId(), null, QUINTA, SEXTA);

        assertEquals(Set.of(quinta.getId(), sexta.getId()),
                noPeriodo.stream().map(Agendamento::getId).collect(Collectors.toSet()));
    }

    @Test
    @DisplayName("S5.5 - Período inválido")
    void s5_5_periodoInvalido() {
        assertThrows(IntervaloInvalidoException.class,
                () -> agendamentoService.consultarHistorico(maria.getId(), null, SEXTA, QUINTA));
    }
}
