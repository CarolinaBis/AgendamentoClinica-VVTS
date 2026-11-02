package br.edu.ifsp.mutacao;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - AgendamentoService (valor devolvido pelas operações de procedimento)")
class AgendamentoServiceMutacaoTest extends TesteBase {

    @Test
    @DisplayName("AgendamentoService:125 - adicionarProcedimento devolve o agendamento atualizado (replaced return value with null)")

    void adicionarProcedimentoDevolveOAgendamentoAtualizado() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        Agendamento retorno = agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId());

        assertNotNull(retorno);
        assertEquals(List.of(consulta.getId(), hemograma.getId()), retorno.getProcedimentoIds());
    }
    @Test
    @DisplayName("AgendamentoService:132 - removerProcedimento devolve o agendamento atualizado (replaced return value with null)")
    void removerProcedimentoDevolveOAgendamentoAtualizado() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma);

        Agendamento retorno = agendamentoService.removerProcedimento(ag.getId(), hemograma.getId());

        assertNotNull(retorno);
        assertEquals(List.of(consulta.getId()), retorno.getProcedimentoIds());
    }
}