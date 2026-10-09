package br.edu.ifsp.funcional;

import br.edu.ifsp.dominio.agendamento.Agendamento;
import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.agendamento.StatusAgendamento;
import br.edu.ifsp.dominio.excecao.*;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.suporte.TesteBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Agendar consulta (US1)")
class AgendarConsultaFuncionalTest extends TesteBase {

    @Test
    @DisplayName("EP procedimentos: um procedimento válido é aceito")
    void umProcedimentoValido() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        assertEquals(List.of(consulta.getId()), agendamentoAtual(ag).getProcedimentoIds());
    }

    @Test
    @DisplayName("EP procedimentos: vários procedimentos válidos são aceitos, na ordem informada")
    void variosProcedimentosValidos() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, hemograma, eletro);
        assertEquals(List.of(consulta.getId(), hemograma.getId(), eletro.getId()),
                agendamentoAtual(ag).getProcedimentoIds());
    }

    @Test
    @DisplayName("EP procedimentos: procedimento repetido na lista é rejeitado")
    void procedimentoRepetidoNaLista() {
        assertThrows(ProcedimentoDuplicadoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta, consulta));
    }

    @Test
    @DisplayName("EP procedimentos: procedimento que não existe no catálogo é rejeitado")
    void procedimentoInexistenteNoCatalogo() {
        assertThrows(ProcedimentoInvalidoException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), List.of(consulta.getId(), 9999L)));
        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }

    @Test
    @DisplayName("Error guessing: lista de procedimentos com elemento nulo é rejeitada com erro de domínio")
    void listaComElementoNulo() {
        assertThrows(DominioException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), Arrays.asList(consulta.getId(), null)));
    }

    @Test
    @DisplayName("Error guessing: paciente inexistente")
    void pacienteInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> agendamentoService.agendar(9999L, PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), List.of(consulta.getId())));
    }

    @Test
    @DisplayName("Error guessing: paciente nulo")
    void pacienteNulo() {
        assertThrows(DominioException.class,
                () -> agendamentoService.agendar(null, PROFISSIONAL_A,
                        meiaHora(QUARTA, 10, 0), List.of(consulta.getId())));
    }

    @Test
    @DisplayName("Error guessing: profissional nulo é rejeitado com erro de domínio")
    void profissionalNulo() {
        assertThrows(DominioException.class,
                () -> agendamentoService.agendar(maria.getId(), null,
                        meiaHora(QUARTA, 10, 0), List.of(consulta.getId())));
        assertTrue(agendamentos.buscarPorPaciente(maria.getId()).isEmpty());
    }

    @Test
    @DisplayName("Período com fim igual ao início é inválido")
    void periodoComFimIgualAoInicio() {
        assertThrows(PeriodoInvalidoException.class,
                () -> new PeriodoAtendimento(QUARTA.atTime(10, 0), QUARTA.atTime(10, 0)));
    }

    @Test
    @DisplayName("Período com fim antes do início é inválido")
    void periodoComFimAntesDoInicio() {
        assertThrows(PeriodoInvalidoException.class,
                () -> new PeriodoAtendimento(QUARTA.atTime(10, 30), QUARTA.atTime(10, 0)));
    }

    @Test
    @DisplayName("Período que atravessa a meia-noite está fora do horário de funcionamento")
    void periodoQueAtravessaMeiaNoite() {
        PeriodoAtendimento viraODia = new PeriodoAtendimento(QUARTA.atTime(23, 30), QUARTA.plusDays(1).atTime(0, 30));
        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A, viraODia, List.of(consulta.getId())));
    }

    @Test
    @DisplayName("AVL data/hora: início exatamente igual a 'agora' é aceito")
    void inicioIgualAAgora() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, periodo(SEGUNDA, 9, 0, 9, 30), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
    }

    @Test
    @DisplayName("AVL data/hora: início 1 minuto depois de 'agora' é aceito")
    void inicioUmMinutoDepoisDeAgora() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, periodo(SEGUNDA, 9, 1, 9, 30), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
    }

    @Test
    @DisplayName("AVL data/hora: início 1 minuto antes de 'agora' é rejeitado")
    void inicioUmMinutoAntesDeAgora() {
        assertThrows(DataHoraNoPassadoException.class,
                () -> agendar(maria, PROFISSIONAL_A, periodo(SEGUNDA, 8, 59, 9, 30), consulta));
    }

    @ParameterizedTest(name = "{0}:{1} - {2}:{3} => válido={4}")
    @CsvSource({
            "8,0,8,30,true",     // começa exatamente na abertura
            "7,59,8,30,false",   // 1 min antes da abertura
            "17,30,18,0,true",   // termina exatamente no fechamento
            "17,31,18,1,false",  // termina 1 min depois do fechamento
            "17,45,18,15,false", // começa dentro e termina depois do fechamento
            "8,0,18,0,true"      // expediente inteiro
    })
    @DisplayName("AVL expediente (08:00-18:00)")
    void limitesDoExpediente(int hIni, int mIni, int hFim, int mFim, boolean valido) {
        PeriodoAtendimento p = periodo(QUARTA, hIni, mIni, hFim, mFim);
        if (valido) {
            assertEquals(StatusAgendamento.CONFIRMADO, agendar(maria, PROFISSIONAL_A, p, consulta).getStatus());
        } else {
            assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                    () -> agendar(maria, PROFISSIONAL_A, p, consulta));
        }
    }

    @ParameterizedTest(name = "{0} (dia útil) é aceito")
    @CsvSource({"2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08", "2026-10-09"})
    @DisplayName("EP dia da semana: segunda a sexta")
    void diasUteis(LocalDate dia) {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(dia, 15, 0), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, ag.getStatus());
    }

    @ParameterizedTest(name = "{0} (fim de semana) é rejeitado")
    @CsvSource({"2026-10-10", "2026-10-11"})
    @DisplayName("EP dia da semana: sábado e domingo")
    void fimDeSemana(LocalDate dia) {
        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(dia, 10, 0), consulta));
    }

    @ParameterizedTest(name = "profissional: {0}:{1}-{2}:{3} conflita={4}")
    @CsvSource(delimiter = ';', textBlock = """
            9,0,10,0,false
            11,0,12,0,false
            9,30,10,30,true
            10,30,11,30,true
            10,15,10,45,true
            9,0,12,0,true
            10,0,11,0,true
            """)
    @DisplayName("Sobreposição na agenda do PROFISSIONAL (existente: 10:00-11:00)")
    void sobreposicaoNaAgendaDoProfissional(String linha) {
        String[] c = linha.split(",");
        agendar(maria, PROFISSIONAL_A, periodo(QUARTA, 10, 0, 11, 0), consulta);
        PeriodoAtendimento novo = periodo(QUARTA, Integer.parseInt(c[0]), Integer.parseInt(c[1]),
                Integer.parseInt(c[2]), Integer.parseInt(c[3]));

        if (Boolean.parseBoolean(c[4])) {
            assertThrows(ConflitoAgendaProfissionalException.class,
                    () -> agendar(joao, PROFISSIONAL_A, novo, hemograma));
        } else {
            assertEquals(StatusAgendamento.CONFIRMADO, agendar(joao, PROFISSIONAL_A, novo, hemograma).getStatus());
        }
    }

    @ParameterizedTest(name = "paciente: {0}")
    @CsvSource(delimiter = ';', textBlock = """
            9,0,10,0,false
            11,0,12,0,false
            9,30,10,30,true
            10,30,11,30,true
            10,15,10,45,true
            9,0,12,0,true
            10,0,11,0,true
            """)
    @DisplayName("Sobreposição na agenda do PACIENTE (existente: 10:00-11:00 com outro profissional)")
    void sobreposicaoNaAgendaDoPaciente(String linha) {
        String[] c = linha.split(",");
        agendar(maria, PROFISSIONAL_A, periodo(QUARTA, 10, 0, 11, 0), consulta);
        PeriodoAtendimento novo = periodo(QUARTA, Integer.parseInt(c[0]), Integer.parseInt(c[1]),
                Integer.parseInt(c[2]), Integer.parseInt(c[3]));

        if (Boolean.parseBoolean(c[4])) {
            assertThrows(ConflitoAgendaPacienteException.class,
                    () -> agendar(maria, PROFISSIONAL_B, novo, hemograma));
        } else {
            assertEquals(StatusAgendamento.CONFIRMADO, agendar(maria, PROFISSIONAL_B, novo, hemograma).getStatus());
        }
    }

    @Test
    @DisplayName("Outro profissional e outro paciente no mesmo horário: sem conflito")
    void mesmoHorarioOutroProfissionalOutroPaciente() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        Agendamento outro = agendar(joao, PROFISSIONAL_B, meiaHora(QUARTA, 10, 0), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, outro.getStatus());
    }

    @Test
    @DisplayName("Agendamento cancelado libera o horário do profissional")
    void canceladoLiberaHorarioDoProfissional() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendamentoService.cancelar(ag.getId());

        Agendamento novo = agendar(joao, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, novo.getStatus());
    }

    @Test
    @DisplayName("Agendamento cancelado libera o horário do paciente")
    void canceladoLiberaHorarioDoPaciente() {
        Agendamento ag = agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        agendamentoService.cancelar(ag.getId());

        Agendamento novo = agendar(maria, PROFISSIONAL_B, meiaHora(QUARTA, 10, 0), consulta);
        assertEquals(StatusAgendamento.CONFIRMADO, novo.getStatus());
    }

    @Test
    @DisplayName("Precedência: sem procedimento + data no passado => procedimento obrigatório")
    void precedenciaProcedimentoSobrePassado() {
        assertThrows(ProcedimentoObrigatorioException.class,
                () -> agendamentoService.agendar(maria.getId(), PROFISSIONAL_A,
                        meiaHora(SEGUNDA.minusDays(1), 10, 0), List.of()));
    }

    @Test
    @DisplayName("Precedência: data no passado + fora do expediente => data no passado")
    void precedenciaPassadoSobreExpediente() {
        assertThrows(DataHoraNoPassadoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(SEGUNDA.minusDays(1), 22, 0), consulta));
    }

    @Test
    @DisplayName("Precedência: fora do expediente + paciente bloqueado => fora do expediente")
    void precedenciaExpedienteSobreBloqueio() {
        bloquear(maria);
        assertThrows(ForaDoHorarioDeFuncionamentoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(SABADO, 10, 0), consulta));
    }

    @Test
    @DisplayName("Precedência: paciente bloqueado + conflito de agenda => bloqueio")
    void precedenciaBloqueioSobreConflito() {
        agendar(joao, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        bloquear(maria);
        assertThrows(PacienteBloqueadoException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta));
    }

    @Test
    @DisplayName("Conflito simultâneo de paciente e de profissional é rejeitado como conflito de agenda")
    void conflitoDePacienteEProfissionalAoMesmoTempo() {
        agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), consulta);
        assertThrows(ConflitoDeAgendaException.class,
                () -> agendar(maria, PROFISSIONAL_A, meiaHora(QUARTA, 10, 0), hemograma));
    }

    private void bloquear(br.edu.ifsp.dominio.paciente.Paciente paciente) {
        for (int i = 0; i < politica.faltas().limite(); i++) {
            paciente.registrarFalta(new Falta(500L + i, AGORA.minusDays(5L + i)));
        }
        pacientes.salvar(paciente);
    }
}
