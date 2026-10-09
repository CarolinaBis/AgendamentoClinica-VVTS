package br.edu.ifsp.funcional;

import br.edu.ifsp.aplicacao.AgendaDoDia;
import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.IntervaloInvalidoException;
import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Consultas de agenda (US4) e histórico (US5)")
class ConsultasFuncionalTest extends TesteBase {

    private Set<Long> ids(List<Agendamento> lista) {
        return lista.stream().map(Agendamento::getId).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Agenda não mistura profissionais")
    void agendaNaoMisturaProfissionais() {
        Agendamento a = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendar(joao, PROFISSIONAL_B, meiaHora(QUARTA, 10, 0), consulta);

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA);

        assertEquals(Set.of(a.getId()), ids(agenda.agendamentos()));
    }

    @Test
    @DisplayName("Agenda não traz agendamentos de outros dias")
    void agendaNaoTrazOutrosDias() {
        Agendamento quarta = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendar(joao, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), consulta);

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA);

        assertEquals(Set.of(quarta.getId()), ids(agenda.agendamentos()));
    }

    @Test
    @DisplayName("Agenda inclui consultas realizadas e faltas do dia (só cancelados ficam de fora)")
    void agendaIncluiRealizadosEFaltas() {
        persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 7, 0), StatusAgendamento.REALIZADO, consulta.getId());
        persistirAgendamento(joao, PROFISSIONAL_A, meiaHora(SEGUNDA, 7, 30), StatusAgendamento.NAO_COMPARECEU, consulta.getId());
        persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0), StatusAgendamento.CANCELADO, consulta.getId());

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, SEGUNDA);

        assertEquals(2, agenda.agendamentos().size());
        assertTrue(agenda.agendamentos().stream().noneMatch(a -> a.getStatus() == StatusAgendamento.CANCELADO));
    }

    @Test
    @DisplayName("Sexta-feira tem expediente; sábado e domingo não")
    void expedienteNosDiasDaSemana() {
        assertTrue(agendamentoService.consultarAgenda(PROFISSIONAL_A, SEXTA).expediente());
        assertFalse(agendamentoService.consultarAgenda(PROFISSIONAL_A, SABADO).expediente());
        assertFalse(agendamentoService.consultarAgenda(PROFISSIONAL_A, DOMINGO).expediente());
    }

    @Test
    @DisplayName("Data omitida usa o dia atual do relógio (que avança com o tempo)")
    void dataOmitidaUsaODiaAtual() {
        Agendamento terca = agendar(maria, PROFISSIONAL_A, meiaHora(TERCA, 10, 0), consulta);
        relogio.definir(TERCA.atTime(8, 0));

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, null);

        assertEquals(TERCA, agenda.data());
        assertEquals(Set.of(terca.getId()), ids(agenda.agendamentos()));
    }

    @Test
    @DisplayName("Data omitida em dia sem expediente retorna lista vazia com aviso")
    void dataOmitidaEmDiaSemExpediente() {
        relogio.definir(SABADO.atTime(10, 0));

        AgendaDoDia agenda = agendamentoService.consultarAgenda(PROFISSIONAL_A, null);

        assertFalse(agenda.expediente());
        assertTrue(agenda.agendamentos().isEmpty());
        assertEquals("Não há expediente nesse dia.", agenda.mensagem());
    }

    @Test
    @DisplayName("Agenda é devolvida em ordem de horário mesmo com vários agendamentos")
    void agendaOrdenadaPorHorario() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 16, 0), consulta);
        agendar(joao, PROFISSIONAL_A, meiaHora(QUARTA, 8, 0), consulta);
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 12, 0), hemograma);

        List<Integer> horas = agendamentoService.consultarAgenda(PROFISSIONAL_A, QUARTA).agendamentos().stream()
                .map(a -> a.getPeriodo().inicio().getHour()).toList();

        assertEquals(List.of(8, 12, 16), horas);
    }

    @Test
    @DisplayName("Histórico não mistura pacientes")
    void historicoNaoMisturaPacientes() {
        Agendamento a = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendar(joao, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), consulta);

        assertEquals(Set.of(a.getId()), ids(agendamentoService.consultarHistorico(maria.getId(), null, null, null)));
    }

    @Test
    @DisplayName("AVL período: data inicial = data final retorna o dia inteiro (limites inclusivos)")
    void periodoDeUmUnicoDia() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 8, 0), consulta);
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 17, 30), hemograma);
        agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 8, 0), eletro);

        assertEquals(2, agendamentoService.consultarHistorico(maria.getId(), null, QUARTA, QUARTA).size());
    }

    @Test
    @DisplayName("Filtro só com data inicial")
    void apenasDataInicial() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        Agendamento quinta = agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), hemograma);
        Agendamento sexta = agendar(maria, PROFISSIONAL_A, meiaHora(SEXTA, 10, 0), eletro);

        assertEquals(Set.of(quinta.getId(), sexta.getId()),
                ids(agendamentoService.consultarHistorico(maria.getId(), null, QUINTA, null)));
    }

    @Test
    @DisplayName("Filtro só com data final")
    void apenasDataFinal() {
        Agendamento quarta = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        Agendamento quinta = agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), hemograma);
        agendar(maria, PROFISSIONAL_A, meiaHora(SEXTA, 10, 0), eletro);

        assertEquals(Set.of(quarta.getId(), quinta.getId()),
                ids(agendamentoService.consultarHistorico(maria.getId(), null, null, QUINTA)));
    }

    @Test
    @DisplayName("Período sem nenhum agendamento retorna lista vazia")
    void periodoSemResultados() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertTrue(agendamentoService.consultarHistorico(maria.getId(), null, SEXTA, SEXTA.plusDays(7)).isEmpty());
    }

    @Test
    @DisplayName("Filtros de status e período combinados")
    void statusEPeriodoCombinados() {
        Agendamento quarta = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        Agendamento quinta = agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), hemograma);
        agendamentoService.cancelar(quarta.getId());
        agendamentoService.cancelar(quinta.getId());
        agendar(maria, PROFISSIONAL_A, meiaHora(SEXTA, 10, 0), eletro);

        List<Agendamento> resultado = agendamentoService
                .consultarHistorico(maria.getId(), StatusAgendamento.CANCELADO, QUINTA, SEXTA);

        assertEquals(Set.of(quinta.getId()), ids(resultado));
    }

    @Test
    @DisplayName("Histórico vem em ordem cronológica")
    void historicoEmOrdemCronologica() {
        agendar(maria, PROFISSIONAL_A, meiaHora(SEXTA, 10, 0), consulta);
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), hemograma);
        agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), eletro);

        List<java.time.LocalDate> dias = agendamentoService.consultarHistorico(maria.getId(), null, null, null)
                .stream().map(a -> a.getPeriodo().data()).toList();

        assertEquals(List.of(QUARTA, QUINTA, SEXTA), dias);
    }

    @Test
    @DisplayName("Error guessing: histórico de paciente inexistente")
    void historicoDePacienteInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> agendamentoService.consultarHistorico(9999L, null, null, null));
    }

    @Test
    @DisplayName("Intervalo invertido é rejeitado mesmo combinado com outros filtros")
    void intervaloInvertidoComStatus() {
        assertThrows(IntervaloInvalidoException.class,
                () -> agendamentoService.consultarHistorico(maria.getId(), StatusAgendamento.CONFIRMADO, SEXTA, QUARTA));
    }
}
