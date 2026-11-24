package br.edu.ifsp.persistencia;

import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

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

    @Test
    @DisplayName("Buscar um procedimento através de um id não existente e devolver vazio")
    void buscarIdProcedimentoNaoExistente(){
        Optional<Procedimento> resultado = repositorio.buscarPorId(99999L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("existePorId é verdadeiro para um procedimento salvo e falso para um id inexistente")
    void existePorIdVerdadeParaSalvoEFalsoParaInexistente(){
        Procedimento salvo = repositorio.salvar(new Procedimento(null, "Consulta clínica"));

        assertThat(repositorio.existePorId(salvo.getId())).isTrue();
        assertThat(repositorio.existePorId(99999L)).isFalse();
    }

    @Test
    @DisplayName("existePorId devolve falso quando o id é nulo")
    void existePorIdDevolveFalsoQuandoOIdENulo(){

        assertThat(repositorio.existePorId(null)).isFalse();
    }

    @Test
    @DisplayName("listar devolve os procedimentos na ordem em que foram cadastrados")
    void listarDevolveOsProcedimentosNaOrdemEmQueForamCadastrados(){
        repositorio.salvar(new Procedimento(null, "Raio-X"));
        repositorio.salvar(new Procedimento(null, "Exame de Sangue"));

        assertThat(repositorio.listar())
                .extracting(Procedimento::getNome)
                .contains("Raio-X", "Exame de Sangue");
    }

    @Test
    @DisplayName("Salvar um procedimento com id existente atualiza o nome sem criar outro")
    void salvarComIdExistenteAtualizaSemDuplicar() {
        Procedimento salvo = repositorio.salvar(new Procedimento(null, "Consulta"));

        repositorio.salvar(new Procedimento(salvo.getId(), "Consulta clínica"));

        assertThat(repositorio.listar())
                .singleElement()
                .extracting(Procedimento::getNome)
                .isEqualTo("Consulta clínica");
    }


}