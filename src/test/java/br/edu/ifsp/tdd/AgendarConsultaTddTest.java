package br.edu.ifsp.tdd;

import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US1 - Agendar consulta")
class AgendarConsultaTddTest extends TesteBase {

    @Test
    @DisplayName("S1.1 - Agendamento confirmado com sucesso")
    void s1_1_agendamentoConfirmadoComSucesso() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        assertNotNull(ag.getId());
        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
        assertEquals(StatusAgendamento.CONFIRMADO,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("S1.2 - Conflito de horário")
    void s1_2_conflitoDeHorario() {
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        ConflitoAgendaProfissionalException ex = assertThrows(ConflitoAgendaProfissionalException.class,
                () -> agendamentoService.agendar(joao.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 15), List.of(consulta.getId())));

        assertTrue(ex.getMessage().toLowerCase().contains("conflito"));
        assertTrue(agendamentos.buscarPorPaciente(joao.getId()).isEmpty());
    }

    @Test
    @DisplayName("S1.3 - Bloqueio por excesso de faltas")
    void s1_3_bloqueioPorExcessoDeFaltas() {
        for (int i = 0; i < 3; i++) {
            maria.registrarFalta(new Falta(100L + i, AGORA.minusDays(10L + i)));
        }
        pacientes.salvar(maria);

        assertThrows(PacienteBloqueadoException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), List.of(consulta.getId())));

        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }

    @Test
    @DisplayName("S1.4 - Procedimento obrigatório")
    void s1_4_procedimentoObrigatorio() {
        assertThrows(ProcedimentoObrigatorioException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), List.of()));

        assertThrows(ProcedimentoObrigatorioException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), null));

        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }

    @Test
    @DisplayName("S1.5 - Fora do horário de funcionamento")
    void s1_5_foraDoHorarioDeFuncionamento() {
        PeriodoAtendimento antesDeAbrir = periodo(QUARTA, 7, 30, 8, 30);

        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        antesDeAbrir, List.of(consulta.getId())));

        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }

    @Test
    @DisplayName("S1.6 - Conflito de agenda do próprio paciente")
    void s1_6_conflitoDeAgendaDoProprioPaciente() {
        agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        assertThrows(ConflitoAgendaPacienteException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_B,
                        meiaHora(QUARTA, 10, 15), List.of(hemograma.getId())));

        assertEquals(1, agendamentos.buscarPorPaciente(maria.getId()).size());
    }

    @Test
    @DisplayName("S1.7 - Agendamento em data/hora no passado")
    void s1_7_agendamentoEmDataHoraNoPassado() {
        PeriodoAtendimento ontem = meiaHora(SEGUNDA.minusDays(1), 10, 0);

        assertThrows(DataHoraNoPassadoException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        ontem, List.of(consulta.getId())));

        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }
}

