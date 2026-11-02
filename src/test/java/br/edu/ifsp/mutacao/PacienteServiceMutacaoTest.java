package br.edu.ifsp.mutacao;

import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - PacienteService")
class PacienteServiceMutacaoTest extends TesteBase {

    @Test
    @DisplayName("PacienteService:28 - cadastrar devolve o paciente salvo (replaced return value with null)")
    void cadastrarDevolveOPacienteSalvo() {
        Paciente cadastrado = pacienteService.cadastrar("Ana");

        assertNotNull(cadastrado);
        assertNotNull(cadastrado.getId());
        assertEquals("Ana", cadastrado.getNome());
        assertEquals("Ana", pacientes.buscarPorId(cadastrado.getId()).orElseThrow().getNome());
    }
}
