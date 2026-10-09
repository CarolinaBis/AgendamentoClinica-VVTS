package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.ProcedimentoNaoEncontradoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.dominio.excecao.UltimoProcedimentoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US7 - Remover procedimento do agendamento")
class RemoverProcedimentoTddTest extends TesteBase {

    private List<Long> procedimentosPersistidos(Long agendamentoId) {
        return agendamentos.buscarPorId(agendamentoId).orElseThrow().getProcedimentoIds();
    }

    @Test
    @DisplayName("S7.1 - Remoção bem-sucedida")
    void s7_1_remocaoBemSucedida() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId(), hemograma.getId()));

        agendamentoService.removerProcedimento(ag.getId(), hemograma.getId());

        assertEquals(List.of(consulta.getId()), procedimentosPersistidos(ag.getId()));
    }

    @Test
    @DisplayName("S7.2 - Remoção do único procedimento")
    void s7_2_remocaoDoUnicoProcedimento() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        assertThrows(UltimoProcedimentoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), consulta.getId()));

        assertEquals(List.of(consulta.getId()), procedimentosPersistidos(ag.getId()));
    }

    @Test
    @DisplayName("S7.3 - Remoção em agendamento já realizado")
    void s7_3_remocaoEmAgendamentoJaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.REALIZADO, consulta.getId(), hemograma.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), hemograma.getId()));

        assertEquals(2, procedimentosPersistidos(ag.getId()).size());
    }

    @Test
    @DisplayName("S7.4 - Procedimento não encontrado")
    void s7_4_procedimentoNaoEncontrado() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId(), hemograma.getId()));

        assertThrows(ProcedimentoNaoEncontradoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), eletro.getId()));

        assertEquals(2, procedimentosPersistidos(ag.getId()).size());
    }

    @Test
    @DisplayName("S7.5 - Remoção em agendamento cancelado")
    void s7_5_remocaoEmAgendamentoCancelado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0),
                StatusAgendamento.CANCELADO, consulta.getId(), hemograma.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), hemograma.getId()));

        assertEquals(2, procedimentosPersistidos(ag.getId()).size());
    }
}
