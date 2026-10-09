package br.edu.ifsp.tdd;

import br.edu.ifsp.aplicacao.SituacaoDoPaciente;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UnitTest")
@Tag("TDD")
@DisplayName("US10 - Consultar contagem de faltas")
class ConsultarFaltasTddTest extends TesteBase {

    @Test
    @DisplayName("S10.1 - Paciente com faltas registradas")
    void s10_1_pacienteComFaltasRegistradas() {
        maria.registrarFalta(new Falta(101L, AGORA.minusDays(30)));
        maria.registrarFalta(new Falta(102L, AGORA.minusDays(5)));
        pacientes.salvar(maria);

        SituacaoDoPaciente situacao = pacienteService.consultarSituacao(maria.getId());

        assertEquals(2, situacao.faltasVigentes());
        assertFalse(situacao.bloqueado());
    }

    @Test
    @DisplayName("S10.2 - Paciente sem faltas")
    void s10_2_pacienteSemFaltas() {
        SituacaoDoPaciente situacao = pacienteService.consultarSituacao(joao.getId());

        assertEquals(0, situacao.faltasVigentes());
        assertFalse(situacao.bloqueado());
    }

    @Test
    @DisplayName("S10.3 - Paciente bloqueado")
    void s10_3_pacienteBloqueado() {
        for (int i = 0; i < 3; i++) {
            maria.registrarFalta(new Falta(100L + i, AGORA.minusDays(10L + i)));
        }
        pacientes.salvar(maria);

        SituacaoDoPaciente situacao = pacienteService.consultarSituacao(maria.getId());

        assertTrue(situacao.bloqueado());
        assertEquals(3, situacao.faltasVigentes());
    }

    @Test
    @DisplayName("S10.4 - Falta expirada não conta")
    void s10_4_faltaExpiradaNaoConta() {
        maria.registrarFalta(new Falta(101L, AGORA.minusMonths(7)));
        maria.registrarFalta(new Falta(102L, AGORA.minusMonths(1)));
        pacientes.salvar(maria);

        SituacaoDoPaciente situacao = pacienteService.consultarSituacao(maria.getId());

        assertEquals(1, situacao.faltasVigentes());
    }
}
