package br.edu.ifsp.tdd;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US9 - Registrar não comparecimento")
class RegistrarFaltaTddTest extends TesteBase {

    private Paciente mariaPersistida() {
        return pacientes.buscarPorId(maria.getId()).orElseThrow();
    }

    @Test
    @DisplayName("S9.1 - Registro de falta bem-sucedido")
    void s9_1_registroDeFaltaBemSucedido() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUARTA, 10, 0), List.of(consulta.getId()));
        relogio.definir(QUARTA.atTime(10, 40));

        Agendamento resultado = agendamentoService.registrarFalta(ag.getId());

        assertEquals(StatusAgendamento.NAO_COMPARECEU, resultado.getStatus());
        assertEquals(StatusAgendamento.NAO_COMPARECEU,
                agendamentos.buscarPorId(ag.getId()).orElseThrow().getStatus());
        assertEquals(1, mariaPersistida().faltasVigentes(relogio.agora(), politica.faltas()));
    }

    @Test
    @DisplayName("S9.2 - Registro de falta em agendamento já realizado")
    void s9_2_registroDeFaltaEmAgendamentoJaRealizado() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.REALIZADO, consulta.getId());

        assertThrows(StatusInvalidoException.class, () -> agendamentoService.registrarFalta(ag.getId()));

        assertEquals(0, mariaPersistida().getFaltas().size());
    }

    @Test
    @DisplayName("S9.3 - Cancelamento antecipado não gera falta")
    void s9_3_cancelamentoAntecipadoNaoGeraFalta() {
        Agendamento ag = agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                meiaHora(QUINTA, 10, 0), List.of(consulta.getId()));

        agendamentoService.cancelar(ag.getId());

        assertTrue(mariaPersistida().getFaltas().isEmpty());
    }

    @Test
    @DisplayName("S9.4 - Bloqueio automático por excesso de faltas")
    void s9_4_bloqueioAutomaticoPorExcessoDeFaltas() {
        // duas faltas antigas ainda vigentes; a terceira atinge o limite
        maria.registrarFalta(new Falta(101L, AGORA.minusDays(20)));
        maria.registrarFalta(new Falta(102L, AGORA.minusDays(10)));
        pacientes.salvar(maria);
        assertFalse(mariaPersistida().estaBloqueado(AGORA, politica.faltas()));

        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.registrarFalta(ag.getId());

        assertTrue(mariaPersistida().estaBloqueado(relogio.agora(), politica.faltas()));
    }
}
