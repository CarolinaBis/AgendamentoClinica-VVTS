package br.edu.ifsp.funcional;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.DataHoraNoPassadoException;
import br.edu.ifsp.dominio.excecao.ForaDoHorarioDeFuncionamentoException;
import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Remarcar (US2) e Cancelar (US3)")
class RemarcarCancelarFuncionalTest extends TesteBase {

    @Test
    @DisplayName("Remarcar para período que sobrepõe o PRÓPRIO período atual não é conflito")
    void remarcarSobrepondoOProprioPeriodo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, periodo(QUARTA, 10, 0, 10, 30), consulta);

        Agendamento remarcado = agendamentoService.remarcar(ag.getId(), periodo(QUARTA, 10, 15, 10, 45));

        assertEquals(periodo(QUARTA, 10, 15, 10, 45), remarcado.getPeriodo());
    }

    @Test
    @DisplayName("Remarcar para exatamente o mesmo período é aceito")
    void remarcarParaOMesmoPeriodo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        Agendamento remarcado = agendamentoService.remarcar(ag.getId(), meiaHora(QUARTA, 10, 0));

        assertEquals(StatusAgendamento.CONFIRMADO, remarcado.getStatus());
    }

    @Test
    @DisplayName("Remarcar libera o horário antigo para outros pacientes")
    void remarcarLiberaHorarioAntigo() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 0));

        Agendamento outro = agendar(joao, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertEquals(StatusAgendamento.CONFIRMADO, outro.getStatus());
    }

    @Test
    @DisplayName("Remarcar preserva paciente, profissional, status e procedimentos")
    void remarcarPreservaDemaisDados() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma);

        agendamentoService.remarcar(ag.getId(), meiaHora(QUINTA, 14, 0));

        Agendamento salvo = agendamentoAtual(ag);
        assertEquals(maria.getId(), salvo.getPacienteId());
        assertEquals(PROFISSIONAL_A, salvo.getProfissionalId());
        assertEquals(StatusAgendamento.CONFIRMADO, salvo.getStatus());
        assertEquals(2, salvo.getProcedimentoIds().size());
    }

    @Test
    @DisplayName("Remarcar para o passado é rejeitado e mantém o período original")
    void remarcarParaOPassado() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(DataHoraNoPassadoException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(SEGUNDA.minusDays(3), 10, 0)));

        assertEquals(meiaHora(QUARTA, 10, 0), agendamentoAtual(ag).getPeriodo());
    }

    @Test
    @DisplayName("Remarcar para fim de semana é rejeitado")
    void remarcarParaFimDeSemana() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);

        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendamentoService.remarcar(ag.getId(), meiaHora(SABADO, 10, 0)));
    }

    @Test
    @DisplayName("Error guessing: remarcar agendamento inexistente")
    void remarcarInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> agendamentoService.remarcar(9999L, meiaHora(QUARTA, 10, 0)));
    }

    @ParameterizedTest(name = "antecedência de {0} min => {1} falta(s)")
    @CsvSource({
            "1500,0",  // 25h
            "1441,0",  // 1 min acima do prazo
            "1440,0",  // exatamente no prazo: "igual ou superior" => sem penalidade
            "1439,1",  // 1 min abaixo do prazo
            "60,1",
            "0,1",     // no instante do início
            "-30,1"    // 30 min depois do início (agendamento ainda Confirmado)
    })
    @DisplayName("AVL prazo mínimo de cancelamento (24h)")
    void limiteDoPrazoDeCancelamento(long antecedenciaEmMinutos, int faltasEsperadas) {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        relogio.definir(ag.getPeriodo().inicio().minusMinutes(antecedenciaEmMinutos));

        Agendamento cancelado = agendamentoService.cancelar(ag.getId());

        assertEquals(StatusAgendamento.CANCELADO, cancelado.getStatus());
        assertEquals(faltasEsperadas, pacienteAtual(maria).faltasVigentes(relogio.agora(), politica.faltas()));
    }

    @Test
    @DisplayName("Cancelar duas vezes: a segunda é rejeitada e a falta não é duplicada")
    void cancelarDuasVezes() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(TERCA, 8, 30), consulta); // tardio
        agendamentoService.cancelar(ag.getId());

        assertThrows(StatusInvalidoException.class, () -> agendamentoService.cancelar(ag.getId()));

        assertEquals(1, pacienteAtual(maria).getFaltas().size());
    }

    @Test
    @DisplayName("Cancelamento tardio registra a falta com a data da consulta como referência")
    void faltaDoCancelamentoTardioReferenciaADataDaConsulta() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(TERCA, 8, 30), consulta);

        agendamentoService.cancelar(ag.getId());

        var falta = pacienteAtual(maria).getFaltas().get(0);
        assertEquals(ag.getId(), falta.agendamentoId());
        assertEquals(TERCA.atTime(8, 30), falta.dataReferencia());
    }

    @Test
    @DisplayName("Error guessing: cancelar agendamento inexistente")
    void cancelarInexistente() {
        assertThrows(RecursoNaoEncontradoException.class, () -> agendamentoService.cancelar(9999L));
    }

    @Test
    @DisplayName("Cancelar uma consulta não afeta os demais agendamentos do paciente")
    void cancelarNaoAfetaOutros() {
        Agendamento a1 = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        Agendamento a2 = agendar(maria, PROFISSIONAL_A, meiaHora(QUINTA, 10, 0), consulta);

        agendamentoService.cancelar(a1.getId());

        assertEquals(StatusAgendamento.CONFIRMADO, agendamentoAtual(a2).getStatus());
    }
}
