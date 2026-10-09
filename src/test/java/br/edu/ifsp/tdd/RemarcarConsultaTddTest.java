package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.ConflitoAgendaPacienteException;
import br.edu.ifsp.dominio.excecao.ConflitoAgendaProfissionalException;
import br.edu.ifsp.dominio.excecao.ForaDoHorarioDeFuncionamentoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US2 - Remarcar consulta")
class RemarcarConsultaTddTest extends TesteBase {

    private Agendamento agendamentoDaMaria() {
        return agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
    }

    @Test
    @DisplayName("S2.1 - Remarcação bem-sucedida")
    void s2_1_remarcacaoBemSucedida() {
        Agendamento ag = agendamentoDaMaria();
        PeriodoAtendimento novo = meiaHora(QUINTA, 14, 0);

        Agendamento remarcado = agendamentoService.remarcar(ag.getId(), novo);

        assertEquals(novo, remarcado.getPeriodo());
        assertEquals(StatusAgendamento.CONFIRMADO, remarcado.getStatus());
        assertEquals(novo, agendamentos.buscarPorId(ag.getId()).orElseThrow().getPeriodo());
    }

    @Test
    @DisplayName("S2.2 - Conflito no novo horário")
    void s2_2_conflitoNoNovoHorario() {
        Agendamento ag = agendamentoDaMaria();
        agendamentoService.agendar(joao.getId(), PROFISSIONAL_A,
                meiaHora(QUINTA, 14, 0), List.of(consulta.getId()));
        PeriodoAtendimento original = ag.getPeriodo();

        assertThrows(ConflitoAgendaProfissionalException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 15)));

        assertEquals(original, agendamentos.buscarPorId(ag.getId()).orElseThrow().getPeriodo());
    }

    @Test
    @DisplayName("S2.3 - Remarcação de agendamento cancelado")
    void s2_3_remarcacaoDeAgendamentoCancelado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0),
                StatusAgendamento.CANCELADO, consulta.getId());

        StatusInvalidoException ex = assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 0)));

        assertTrue(ex.getMessage().toLowerCase().contains("remarcar"));
        assertTrue(ex.getMessage().toLowerCase().contains("cancelado"));
    }

    @Test
    @DisplayName("S2.4 - Remarcação de agendamento já realizado")
    void s2_4_remarcacaoDeAgendamentoJaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.REALIZADO, consulta.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 0)));

        assertEquals(StatusAgendamento.REALIZADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S2.5 - Novo horário fora do funcionamento da clínica")
    void s2_5_novoHorarioForaDoFuncionamentoDaClinica() {
        Agendamento ag = agendamentoDaMaria();

        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 19, 0)));

        assertEquals(meiaHora(QUARTA, 10, 0),
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getPeriodo());
    }

    @Test
    @DisplayName("S2.6 - Novo horário conflita com outro agendamento do próprio paciente")
    void s2_6_novoHorarioConflitaComOutroAgendamentoDoProprioPaciente() {
        Agendamento ag = agendamentoDaMaria();
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_B,
                meiaHora(QUINTA, 14, 0), List.of(hemograma.getId()));

        assertThrows(ConflitoAgendaPacienteException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 15)));
    }
}
