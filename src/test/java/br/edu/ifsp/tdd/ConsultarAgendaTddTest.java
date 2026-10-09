package br.edu.ifsp.tdd;

import br.edu.ifsp.aplicacao.AgendaDoDia;
import br.edu.ifsp.agendamento.Agendamento;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US4 - Consultar agenda do profissional")
class ConsultarAgendaTddTest extends TesteBase {

    @Test
    @DisplayName("S4.1 - Agenda com consultas no dia")
    void s4_1_agendaComConsultasNoDia() {

        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 11, 0), List.of(consulta.getId()));
        agendamentoService.agendar(joao.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 9, 30), List.of(consulta.getId()));

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA);

        List<LocalDateTime> inicios = agenda.agendamentos().stream().map(a -> a.getPeriodo().inicio()).toList();
        assertEquals(List.of(QUARTA.atTime(9, 30), QUARTA.atTime(11, 0)), inicios);
        assertTrue(agenda.expediente());
    }

    @Test
    @DisplayName("S4.2 - Agenda vazia")
    void s4_2_agendaVazia() {
        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA);

        assertTrue(agenda.agendamentos().isEmpty());
        assertTrue(agenda.expediente());
    }

    @Test
    @DisplayName("S4.3 - Cancelados não aparecem na agenda ativa")
    void s4_3_canceladosNaoAparecemNaAgendaAtiva() {
        Agendamento cancelado = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 9, 0), List.of(consulta.getId()));
        Agendamento ativo = agendamentoService.agendar(joao.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
        agendamentoService.cancelar(cancelado.getId());

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA);

        assertEquals(1, agenda.agendamentos().size());
        assertEquals(ativo.getId(), agenda.agendamentos().get(0).getId());
    }

    @Test
    @DisplayName("S4.4 - Consulta sem informar data")
    void s4_4_consultaSemInformarData() {
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, meiaHora(SEGUNDA, 15, 0), List.of(consulta.getId()));
        agendamentoService.agendar(joao.getId(), PROFISSIONAL_A, meiaHora(QUARTA, 15, 0), List.of(consulta.getId()));

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, null);

        assertEquals(SEGUNDA, agenda.data()); // "hoje" no relógio do teste
        assertEquals(1, agenda.agendamentos().size());
    }

    @Test
    @DisplayName("S4.5 - Consulta em dia sem funcionamento")
    void s4_5_consultaEmDiaSemFuncionamento() {
        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, DOMINGO);

        assertTrue(agenda.agendamentos().isEmpty());
        assertFalse(agenda.expediente());
        assertTrue(agenda.mensagem().toLowerCase().contains("expediente"));
    }
}
