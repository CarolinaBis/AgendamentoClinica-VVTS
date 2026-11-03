package br.edu.ifsp.mutacao;

import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - StatusInvalidoException")
class StatusInvalidoExceptionMutacaoTest {

    @Test
    @DisplayName("getStatus devolve o status informado (replaced return value with null)")
    void getStatusDevolveOStatusInformado() {
        StatusInvalidoException excecao = new StatusInvalidoException("cancelar", StatusAgendamento.REALIZADO);

        assertEquals(StatusAgendamento.REALIZADO, excecao.getStatus());
    }
}
