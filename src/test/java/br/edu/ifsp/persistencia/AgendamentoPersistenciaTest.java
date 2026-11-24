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
import br.edu.ifsp.security.user.Role;
import br.edu.ifsp.security.user.User;
import br.edu.ifsp.security.user.UserRepository;
import org.springframework.dao.DataAccessException;


import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
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

    @Test
    @DisplayName("Não salvar agendamento de um paciente inexistente")
    void naoSalvarAgendamentoDePacienteInexistente(){
        Procedimento consulta = procedimentoRepositorio.salvar(new Procedimento(null, "Consulta clínica"));
        PeriodoAtendimento periodo = new PeriodoAtendimento(
                LocalDateTime.of(2026,12,7,9,0), LocalDateTime.of(2026,12,7,10,0));

        Agendamento agendamento = Agendamento.reconstituir(
                null, 99999L, 10L, periodo, StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(consulta.getId())), null);

        assertThatThrownBy(() -> agendamentoRepositorio.salvar(agendamento))
                .isInstanceOf(DataAccessException.class);
    }


    private Agendamento novoAgendamento(Long pacienteId, Long profissionalId, LocalDateTime inicio, Long procedimentoId) {
        return Agendamento.reconstituir(null, pacienteId, profissionalId,
                new PeriodoAtendimento(inicio, inicio.plusHours(1)), StatusAgendamento.CONFIRMADO,
                List.of(ProcedimentoAgendado.pendente(procedimentoId)), null);
    }



    @Test
    @DisplayName("Buscar por profissional devolve só os agendamentos dele, ordenados pelo início")
    void buscarPorProfissionalFiltraEOrdenaPeloInicio() {
        Paciente paciente = pacienteRepositorio.salvar(Paciente.novo("Ana"));
        Procedimento consulta = procedimentoRepositorio.salvar(new Procedimento(null, "Consulta clínica"));
        LocalDateTime tarde = LocalDateTime.of(2026, 12, 7, 14, 0);
        LocalDateTime manha = LocalDateTime.of(2026, 12, 7, 9, 0);
        agendamentoRepositorio.salvar(novoAgendamento(paciente.getId(), 10L, tarde, consulta.getId()));
        agendamentoRepositorio.salvar(novoAgendamento(paciente.getId(), 10L, manha, consulta.getId()));
        agendamentoRepositorio.salvar(novoAgendamento(paciente.getId(), 20L, manha, consulta.getId()));

        List<Agendamento> agenda = agendamentoRepositorio.buscarPorProfissional(10L);

        assertThat(agenda)
                .extracting(agendamento -> agendamento.getPeriodo().inicio())
                .containsExactly(manha, tarde);
    }
}