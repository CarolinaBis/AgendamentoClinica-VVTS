package br.edu.ifsp.mutacao;

import br.edu.ifsp.aplicacao.ProcedimentoService;
import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.suporte.ProcedimentoRepositoryEmMemoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - ProcedimentoService")
class ProcedimentoServiceMutacaoTest {

    private final ProcedimentoRepositoryEmMemoria repositorio = new ProcedimentoRepositoryEmMemoria();
    private final ProcedimentoService servico = new ProcedimentoService(repositorio);

    @Test
    @DisplayName("ProcedimentoService:21 - cadastrar devolve o procedimento salvo (replaced return value with null)")
    void cadastrarDevolveOProcedimentoSalvo() {
        Procedimento cadastrado = servico.cadastrar("Raio-X");

        assertNotNull(cadastrado);
        assertNotNull(cadastrado.getId());
        assertEquals("Raio-X", cadastrado.getNome());
        assertTrue(repositorio.existePorId(cadastrado.getId()));
    }

    @Test
    @DisplayName("ProcedimentoService:25 - listar devolve os procedimentos cadastrados (replaced return value with emptyList)")
    void listarDevolveOsProcedimentosCadastrados() {
        servico.cadastrar("Raio-X");
        servico.cadastrar("Ultrassom");

        List<String> nomes = servico.listar().stream().map(Procedimento::getNome).toList();

        assertEquals(List.of("Raio-X", "Ultrassom"), nomes);
    }
}
