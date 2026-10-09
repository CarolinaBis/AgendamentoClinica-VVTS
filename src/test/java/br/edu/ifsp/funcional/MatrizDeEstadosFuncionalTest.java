package br.edu.ifsp.funcional;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Matriz estado x operação")
class MatrizDeEstadosFuncionalTest extends TesteBase {

    enum Operacao {
        REMARCAR, CANCELAR, ADICIONAR_PROCEDIMENTO, REMOVER_PROCEDIMENTO,
        EXECUTAR_PROCEDIMENTO, CONFIRMAR_REALIZACAO, REGISTRAR_FALTA
    }

    static Stream<Arguments> combinacoesRejeitadas() {
        return Stream.of(StatusAgendamento.CANCELADO, StatusAgendamento.REALIZADO, StatusAgendamento.NAO_COMPARECEU)
                .flatMap(status -> Stream.of(Operacao.values()).map(op -> Arguments.of(status, op)));
    }

    private void executar(Operacao op, Agendamento ag) {
        Long id = ag.getId();
        switch (op) {
            case REMARCAR -> agendamentoService.remarcar(id, meiaHora(QUINTA, 14, 0));
            case CANCELAR -> agendamentoService.cancelar(id);
            case ADICIONAR_PROCEDIMENTO -> agendamentoService.adicionarProcedimento(id, eletro.getId());
            case REMOVER_PROCEDIMENTO -> agendamentoService.removerProcedimento(id, hemograma.getId());
            case EXECUTAR_PROCEDIMENTO -> agendamentoService.marcarProcedimentoComoExecutado(id, consulta.getId());
            case CONFIRMAR_REALIZACAO -> agendamentoService.confirmarRealizacao(id);
            case REGISTRAR_FALTA -> agendamentoService.registrarFalta(id);
        }
    }

    @ParameterizedTest(name = "{0} + {1} => rejeitado")
    @MethodSource("combinacoesRejeitadas")
    @DisplayName("Estados finais rejeitam qualquer operação e permanecem inalterados")
    void estadosFinaisRejeitamOperacoes(StatusAgendamento status, Operacao op) {
        // consulta já no passado (seg 08:00) para que nenhuma regra de horário interfira
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                status, consulta.getId(), hemograma.getId());

        assertThrows(StatusInvalidoException.class, () -> executar(op, ag));

        Agendamento depois = agendamentoAtual(ag);
        assertEquals(status, depois.getStatus());
        assertEquals(List.of(consulta.getId(), hemograma.getId()), depois.getProcedimentoIds());
        assertEquals(meiaHora(SEGUNDA, 8, 0), depois.getPeriodo());
        assertEquals(0, pacienteAtual(maria).getFaltas().size(), "nenhuma falta pode ser criada");
    }

    @ParameterizedTest(name = "CONFIRMADO + {0} => aceito")
    @MethodSource("operacoesAceitasEmConfirmado")
    @DisplayName("Status Confirmado aceita as operações de mudança")
    void confirmadoAceitaOperacoes(Operacao op) {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId(), hemograma.getId());

        assertDoesNotThrow(() -> executar(op, ag));
    }

    static Stream<Operacao> operacoesAceitasEmConfirmado() {
        // CONFIRMAR_REALIZACAO tem pré-condições próprias (procedimentos executados): testada à parte
        return Stream.of(Operacao.REMARCAR, Operacao.CANCELAR, Operacao.ADICIONAR_PROCEDIMENTO,
                Operacao.REMOVER_PROCEDIMENTO, Operacao.EXECUTAR_PROCEDIMENTO, Operacao.REGISTRAR_FALTA);
    }

    @Test
    @DisplayName("Confirmado -> Realizado quando horário atingido e procedimentos executados")
    void confirmadoParaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.marcarProcedimentoComoExecutado(ag.getId(), consulta.getId());

        agendamentoService.confirmarRealizacao(ag.getId());

        assertEquals(StatusAgendamento.REALIZADO, agendamentoAtual(ag).getStatus());
    }
}
