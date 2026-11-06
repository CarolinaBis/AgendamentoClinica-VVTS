package br.edu.ifsp.mutacao;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UnitTest")
@Tag("Mutation")
@DisplayName("Mutação - Procedimento")
class ProcedimentoMutacaoTest {

    @Test
    @DisplayName("getNome devolve o nome sem espaços nas pontas (replaced return value with \"\")")
    void getNomeDevolveONomeAparado() {
        Procedimento procedimento = new Procedimento(1L, "  Hemograma ");

        assertEquals("Hemograma", procedimento.getNome());
    }
}
