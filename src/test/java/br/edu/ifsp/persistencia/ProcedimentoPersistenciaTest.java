package br.edu.ifsp.persistencia;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
class ProcedimentoPersistenciaTest extends PersistenciaTestBase {

    @Autowired
    private ProcedimentoRepository repositorio;

    @Test
    @DisplayName("Salva um procedimento novo gerando o id automaticamente")
    void salvaProcedimentoNovoGerandoId() {
        Procedimento salvo = repositorio.salvar(new Procedimento(null, "Consulta clínica"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(repositorio.buscarPorId(salvo.getId()))
                .hasValueSatisfying(encontrado -> assertThat(encontrado.getNome()).isEqualTo("Consulta clínica"));
    }
}