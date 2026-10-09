package br.edu.ifsp.funcional;

import br.edu.ifsp.aplicacao.SituacaoDoPaciente;
import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.HorarioNaoAtingidoException;
import br.edu.ifsp.dominio.excecao.PacienteBloqueadoException;
import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;


@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Faltas, validade e bloqueio (US9/US10)")
class FaltasFuncionalTest extends TesteBase {

    private void darFaltasRecentes(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            maria.registrarFalta(new Falta(200L + i, AGORA.minusDays(3L + i)));
        }
        pacientes.salvar(maria);
    }

    @ParameterizedTest(name = "{0} falta(s) vigente(s) => bloqueado={1}")
    @CsvSource({"0,false", "1,false", "2,false", "3,true", "4,true"})
    @DisplayName("AVL limite de faltas (limite = 3)")
    void limiteDeFaltas(int faltas, boolean bloqueado) {
        darFaltasRecentes(faltas);

        SituacaoDoPaciente situacao = pacienteService.consultarSituacao(maria.getId());

        assertEquals(faltas, situacao.faltasVigentes());
        assertEquals(bloqueado, situacao.bloqueado());
    }

    @Test
    @DisplayName("Paciente com faltas abaixo do limite ainda consegue agendar")
    void abaixoDoLimiteAindaAgenda() {
        darFaltasRecentes(2);

        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
    }

    @Test
    @DisplayName("Bloqueio de um paciente não afeta os demais")
    void bloqueioNaoAfetaOutrosPacientes() {
        darFaltasRecentes(3);

        Agendamento ag = agendar(joao, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
    }

    @Test
    @DisplayName("AVL validade: falta com exatamente 6 meses ainda é vigente")
    void faltaComExatamenteSeisMeses() {
        maria.registrarFalta(new Falta(301L, AGORA.minusMonths(6)));
        pacientes.salvar(maria);

        assertEquals(1, pacienteService.consultarSituacao(maria.getId()).faltasVigentes());
    }

    @Test
    @DisplayName("AVL validade: falta com 6 meses e 1 segundo expira")
    void faltaComSeisMesesEUmSegundo() {
        maria.registrarFalta(new Falta(301L, AGORA.minusMonths(6).minusSeconds(1)));
        pacientes.salvar(maria);

        assertEquals(0, pacienteService.consultarSituacao(maria.getId()).faltasVigentes());
    }

    @Test
    @DisplayName("Falta que expira desbloqueia o paciente, que volta a poder agendar")
    void expiracaoDesbloqueia() {
        maria.registrarFalta(new Falta(301L, AGORA.minusMonths(6).minusDays(1))); // expirada
        maria.registrarFalta(new Falta(302L, AGORA.minusDays(10)));
        maria.registrarFalta(new Falta(303L, AGORA.minusDays(5)));
        pacientes.salvar(maria);

        assertFalse(pacienteService.consultarSituacao(maria.getId()).bloqueado());
        assertEquals(StatusAgendamento.CONFIRMADO,
                agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta).getStatus());
    }

    @Test
    @DisplayName("O tempo passando faz as faltas expirarem (relógio avança 7 meses)")
    void tempoPassandoExpiraFaltas() {
        darFaltasRecentes(3);
        assertTrue(pacienteService.consultarSituacao(maria.getId()).bloqueado());

        relogio.definir(AGORA.plusMonths(7));

        assertFalse(pacienteService.consultarSituacao(maria.getId()).bloqueado());
    }

    @Test
    @DisplayName("AVL horário: registrar falta no instante exato do início é permitido")
    void faltaNoInstanteDoInicio() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 10, 0), consulta);
        relogio.definir(SEGUNDA.atTime(10, 0));

        assertEquals(StatusAgendamento.NAO_COMPARECEU, agendamentoService.registrarFalta(ag.getId()).getStatus());
    }

    @Test
    @DisplayName("AVL horário: registrar falta 1 segundo antes do início é rejeitado")
    void faltaUmSegundoAntesDoInicio() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 10, 0), consulta);
        relogio.definir(SEGUNDA.atTime(10, 0).minusSeconds(1));

        assertThrows(HorarioNaoAtingidoException.class, () -> agendamentoService.registrarFalta(ag.getId()));

        assertEquals(StatusAgendamento.CONFIRMADO, agendamentoAtual(ag).getStatus());
        assertTrue(pacienteAtual(maria).getFaltas().isEmpty());
    }

    @Test
    @DisplayName("Registrar falta duas vezes: a segunda é rejeitada e o contador não duplica")
    void faltaDuasVezes() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.registrarFalta(ag.getId());

        assertThrows(StatusInvalidoException.class, () -> agendamentoService.registrarFalta(ag.getId()));

        assertEquals(1, pacienteAtual(maria).getFaltas().size());
    }

    @Test
    @DisplayName("Falta registrada guarda o início da consulta como data de referência")
    void faltaGuardaDataDeReferencia() {
        Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());

        agendamentoService.registrarFalta(ag.getId());

        Falta falta = pacienteAtual(maria).getFaltas().get(0);
        assertEquals(ag.getId(), falta.agendamentoId());
        assertEquals(SEGUNDA.atTime(8, 0), falta.dataReferencia());
    }

    @Test
    @DisplayName("Falta por não comparecimento e cancelamento tardio se acumulam")
    void faltaECancelamentoTardioSeAcumulam() {
        Agendamento tardio = agendar(maria, PROFISSIONAL_A, meiaHora(TERCA, 8, 30), consulta);
        agendamentoService.cancelar(tardio.getId());
        Agendamento ausente = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, 8, 0),
                StatusAgendamento.CONFIRMADO, consulta.getId());
        agendamentoService.registrarFalta(ausente.getId());

        assertEquals(2, pacienteService.consultarSituacao(maria.getId()).faltasVigentes());
    }

    @Test
    @DisplayName("Fluxo completo: a 3ª falta registrada bloqueia novos agendamentos")
    void terceiraFaltaBloqueiaNovosAgendamentos() {
        for (int hora = 5; hora <= 7; hora++) {
            Agendamento ag = persistirAgendamento(maria, PROFISSIONAL_A, meiaHora(SEGUNDA, hora, 0),
                    StatusAgendamento.CONFIRMADO, consulta.getId());
            agendamentoService.registrarFalta(ag.getId());
        }

        assertTrue(pacienteService.consultarSituacao(maria.getId()).bloqueado());
        assertThrows(PacienteBloqueadoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta));
    }

    @Test
    @DisplayName("Error guessing: consultar situação de paciente inexistente")
    void situacaoDePacienteInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> pacienteService.consultarSituacao(9999L));
    }

    @Test
    @DisplayName("Error guessing: registrar falta em agendamento inexistente")
    void faltaEmAgendamentoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> agendamentoService.registrarFalta(9999L));
    }
}
