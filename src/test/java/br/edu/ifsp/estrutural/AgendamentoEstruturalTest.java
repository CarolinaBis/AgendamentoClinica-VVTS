package br.edu.ifsp.estrutural;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("Structural")
@DisplayName("Estrutural - Agendamento")
class AgendamentoEstruturalTest {
    @Test
    @DisplayName("definirId rejeita um agendamento que já possui id")
    void definirIdRejeitaAgendamentoQueJaTemId() {
        PeriodoAtendimento periodo = new PeriodoAtendimento(
                LocalDateTime.of(2026, 10, 7, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 30));
        Agendamento agendamento = Agendamento.reconstituir(5L, 1L, 10L, periodo,
                StatusAgendamento.CONFIRMADO, List.of(), null);
        assertThrows(IllegalStateException.class, () -> agendamento.definirId(6L));
        assertEquals(5L, agendamento.getId());
    }
}