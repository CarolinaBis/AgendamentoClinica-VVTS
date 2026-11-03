package br.edu.ifsp.mutacao;

import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - StatusAgendamento")
class StatusAgendamentoMutacaoTest {

    @Test
    @DisplayName("getRotulo devolve o texto de cada status (replaced return value with \"\")")
    void rotuloDeCadaStatus() {
        assertEquals("Confirmado", StatusAgendamento.CONFIRMADO.getRotulo());
        assertEquals("Cancelado", StatusAgendamento.CANCELADO.getRotulo());
        assertEquals("Realizado", StatusAgendamento.REALIZADO.getRotulo());
        assertEquals("Não Compareceu", StatusAgendamento.NAO_COMPARECEU.getRotulo());
    }
}
