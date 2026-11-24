package br.edu.ifsp.persistencia;

import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
class PacientePersistenciaTest extends PersistenciaTestBase {

    @Autowired
    private PacienteRepository repositorio;

    @Test
    @DisplayName("Salva o paciente com as faltas e as recupera ao buscar")
    void salvaOPacienteComAsFaltasEAsRecuperaAoBuscar(){
        Paciente paciente = Paciente.novo("Yasmim");

        paciente.registrarFalta(new Falta(1L, LocalDateTime.of(2026,10,10,9,0)));
        paciente.registrarFalta(new Falta(2L, LocalDateTime.of(2026, 11,12,10,0)));

        Paciente salvo = repositorio.salvar(paciente);

        Paciente encontrado = repositorio.buscarPorId(salvo.getId()).orElseThrow();

        assertThat(encontrado.getNome()).isEqualTo("Yasmim");
        assertThat(encontrado.getFaltas()).hasSize(2);
    }
}