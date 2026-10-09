package br.edu.ifsp.funcional;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.*;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Procedimentos (US6/US7) e realização (US8)")
class ProcedimentosFuncionalTest extends TesteBase {

    private List<Long> procs(Agendamento ag) {
        return agendamentoAtual(ag).getProcedimentoIds();
    }

    @Test
    @DisplayName("Adicionar vários procedimentos em sequência mantém a ordem")
    void adicionarVariosEmSequencia() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId());
        agendamentoService.adicionarProcedimento(ag.getId(), eletro.getId());

        assertEquals(List.of(consulta.getId(), hemograma.getId(), eletro.getId()), procs(ag));
    }

    @Test
    @DisplayName("Remover um procedimento e adicioná-lo de novo é permitido")
    void removerEReadicionar() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma);

        agendamentoService.removerProcedimento(ag.getId(), hemograma.getId());
        agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId());

        assertEquals(List.of(consulta.getId(), hemograma.getId()), procs(ag));
    }

    @Test
    @DisplayName("Remover até sobrar um; o último não pode ser removido")
    void removerAteSobrarUm() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma, eletro);

        agendamentoService.removerProcedimento(ag.getId(), consulta.getId());
        agendamentoService.removerProcedimento(ag.getId(), hemograma.getId());

        assertThrows(UltimoProcedimentoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), eletro.getId()));
        assertEquals(List.of(eletro.getId()), procs(ag));
    }

    @Test
    @DisplayName("Remover procedimento inexistente em agendamento de um só procedimento: 'não encontrado' (e não 'último')")
    void naoEncontradoTemPrecedenciaSobreUltimo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(ProcedimentoNaoEncontradoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), hemograma.getId()));
    }

    @Test
    @DisplayName("Adicionar procedimento duplicado não altera a lista")
    void duplicadoNaoAlteraALista() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma);

        assertThrows(ProcedimentoDuplicadoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId()));

        assertEquals(List.of(consulta.getId(), hemograma.getId()), procs(ag));
    }

    @Test
    @DisplayName("Error guessing: adicionar procedimento nulo")
    void adicionarProcedimentoNulo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(ProcedimentoInvalidoException.class,
                () -> agendamentoService.adicionarProcedimento(ag.getId(), null));
    }

    @Test
    @DisplayName("Error guessing: remover procedimento nulo")
    void removerProcedimentoNulo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma);

        assertThrows(ProcedimentoNaoEncontradoException.class,
                () -> agendamentoService.removerProcedimento(ag.getId(), null));
    }

    @Test
    @DisplayName("Error guessing: adicionar e remover em agendamento inexistente")
    void agendamentoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> agendamentoService.adicionarProcedimento(9999L, consulta.getId()));
        assertThrows(RecursoNaoEncontradoException.class,
                () -> agendamentoService.removerProcedimento(9999L, consulta.getId()));
    }

    @Test
    @DisplayName("Procedimento recém-adicionado nasce pendente e impede a confirmação")
    void procedimentoNovoNasceuPendente() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 9, 30), consulta);
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());
        agendamentoService.adicionarProcedimento(ag.getId(), hemograma.getId());
        relogio.definir(SEGUNDA.atTime(10, 0));

        ProcedimentosPendentesException ex = assertThrows(ProcedimentosPendentesException.class,
                () -> agendamentoService.confirmarRealizacao(ag.getId()));

        assertEquals(List.of(hemograma.getId()), ex.getProcedimentosPendentes());
    }

    @Test
    @DisplayName("Assinalar o mesmo procedimento como executado duas vezes é inofensivo")
    void executarDuasVezes() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());

        assertTrue(agendamentoAtual(ag).getProcedimentos().get(0).isExecutado());
    }

    @Test
    @DisplayName("Error guessing: assinalar execução de procedimento que não pertence ao agendamento")
    void executarProcedimentoDeOutroAgendamento() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(ProcedimentoNaoEncontradoException.class,
                () -> agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), eletro.getId()));
    }

    @Test
    @DisplayName("AVL horário: confirmar no instante exato do início é permitido")
    void confirmarNoInstanteDoInicio() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());
        relogio.definir(QUARTA.atTime(10, 0));

        assertEquals(StatusAgendamento.REALIZADO, agendamentoService.confirmarRealizacao(ag.getId()).getStatus());
    }

    @Test
    @DisplayName("AVL horário: confirmar 1 segundo antes do início é rejeitado")
    void confirmarUmSegundoAntes() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());
        relogio.definir(QUARTA.atTime(10, 0).minusSeconds(1));

        assertThrows(HorarioNaoAtingidoException.class, () -> agendamentoService.confirmarRealizacao(ag.getId()));
    }

    @Test
    @DisplayName("Antes do horário + procedimento pendente: a regra de horário vem primeiro")
    void horarioTemPrecedenciaSobrePendencias() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(HorarioNaoAtingidoException.class, () -> agendamentoService.confirmarRealizacao(ag.getId()));
    }

    @Test
    @DisplayName("Confirmar realização de agendamento marcado como falta é rejeitado")
    void confirmarDepoisDeFalta() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.registrarFalta(ag.getId());

        assertThrows(StatusInvalidoException.class, () -> agendamentoService.confirmarRealizacao(ag.getId()));
    }

    @Test
    @DisplayName("Confirmar realização não gera falta para o paciente")
    void confirmarNaoGeraFalta() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());

        agendamentoService.confirmarRealizacao(ag.getId());

        assertTrue(pacienteAtual(maria).getFaltas().isEmpty());
    }
}
