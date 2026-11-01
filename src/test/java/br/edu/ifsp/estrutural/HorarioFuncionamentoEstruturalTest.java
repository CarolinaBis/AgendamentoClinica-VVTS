package br.edu.ifsp.estrutural;

import br.edu.ifsp.dominio.politica.HorarioFuncionamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("Structural")
@DisplayName("Estrutural - HorarioFuncionamento (validações do construtor)")
class HorarioFuncionamentoEstruturalTest {
    @Test
    @DisplayName("rejeita conjunto de dias nulo")
    void rejeitaDiasNulos() {
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(null, LocalTime.of(8, 0), LocalTime.of(18, 0)));
    }

    @Test
    @DisplayName("rejeita abertura nula")
    void rejeitaAberturaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(Set.of(DayOfWeek.MONDAY), null, LocalTime.of(18, 0)));
    }

    @Test
    @DisplayName("rejeita fechamento nulo")
    void rejeitaFechamentoNulo() {
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(Set.of(DayOfWeek.MONDAY), LocalTime.of(8, 0), null));
    }
}