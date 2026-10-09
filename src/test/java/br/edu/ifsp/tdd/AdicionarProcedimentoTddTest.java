package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.ProcedimentoDuplicadoException;
import br.edu.ifsp.dominio.excecao.ProcedimentoInvalidoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US6 - Adicionar procedimento ao agendamento")
class AdicionarProcedimentoTddTest extends TesteBase {

    private List<Long> procedimentosPersistidos(Long agendamentoId) {
        return agendamentos.buscarPorId(agendamentoId).orElseThrow().getProcedimentoIds();
    }

    @Test
    @DisplayName("S6.1 - Adição bem-sucedida")
    void s6_1_adicaoBemSucedida() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId());

        assertEquals(List.of(consulta.getId(), hemograma.getId()), procedimentosPersistidos(ag.getId()));
    }

    @Test
    @DisplayName("S6.2 - Adição em agendamento já realizado")
    void s6_2_adicaoEmAgendamentoJaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.REALIZADO, consulta.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId()));

        assertEquals(List.of(consulta.getId()), procedimentosPersistidos(ag.getId()));
    }

    @Test
    @DisplayName("S6.3 - Procedimento duplicado")
    void s6_3_procedimentoDuplicado() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        assertThrows(ProcedimentoDuplicadoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), consulta.getId()));

        assertEquals(List.of(consulta.getId()), procedimentosPersistidos(ag.getId()));
    }

    @Test
    @DisplayName("S6.4 - Adição em agendamento cancelado")
    void s6_4_adicaoEmAgendamentoCancelado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0),
                StatusAgendamento.CANCELADO, consulta.getId());

        assertThrows(StatusInvalidoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId()));
    }

    @Test
    @DisplayName("S6.5 - Procedimento fora do catálogo")
    void s6_5_procedimentoForaDoCatalogo() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));

        assertThrows(ProcedimentoInvalidoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), 9999L));

        assertEquals(List.of(consulta.getId()), procedimentosPersistidos(ag.getId()));
    }
}
