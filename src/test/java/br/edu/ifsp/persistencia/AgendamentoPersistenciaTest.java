package br.edu.ifsp.persistencia;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.AgendamentoRepository;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.agendamento.ProcedimentoAgendado;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.paciente.PacienteRepository;
import br.edu.ifsp.dominio.procedimento.Procedimento;
import br.edu.ifsp.dominio.procedimento.ProcedimentoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("PersistenceTest")
@Tag("IntegrationTest")
class AgendamentoPersistenciaTest extends PersistenciaTestBase {

    @Autowired
    private AgendamentoRepository agendamentoRepositorio;
    @Autowired
    private PacienteRepository pacienteRepositorio;
    @Autowired
    private ProcedimentoRepository procedimentoRepositorio;

    @Test
    @DisplayName("Salva o agendamento com os procedimentos na ordem em que foram informados")
    void salvaOAgendamentoComOsProcedimentosNaOrdemEmQueForamInformados(){
        Paciente paciente = pacienteRepositorio.salvar(Paciente.novo("Yasmim"));
        Procedimento consulta = procedimentoRepositorio.salvar(new Procedimento(null, "Consulta clínica"));
        Procedimento exame = procedimentoRepositorio.salvar(new Procedimento(null, "Exame de Sangue"));

        PeriodoAtendimento periodo = new PeriodoAtendimento(LocalDateTime.of(2026, 12, 7, 9,
                0), LocalDateTime.of(2026, 12, 7, 10, 0));

        Agendamento agendamento = Agendamento.reconstituir(
                null, paciente.getId(), 10L, periodo, StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(exame.getId()),
                        ProcedimentoAgendado.pendente(consulta.getId())
                ),
                null
        );
    }

}