package br.edu.ifsp.estrutural;

import br.edu.ifsp.aplicacao.AgendaDoDia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("UnitTest")
@Tag("Structural")
@DisplayName("Estrutural - AgendaDoDia")
class AgendaDoDiaEstruturalTest {
    @Test
    @DisplayName("mensagem() devolve null quando há expediente")
    void mensagemEhNulaQuandoHaExpediente() {
        AgendaDoDia agenda = new AgendaDoDia(LocalDate.of(2026, 10, 7), true, List.of());
        assertNull(agenda.mensagem());
    }
}