package br.edu.ifsp.funcional;

import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;
import br.edu.ifsp.dominio.excecao.PeriodoInvalidoException;
import br.edu.ifsp.dominio.paciente.Falta;
import br.edu.ifsp.dominio.paciente.Paciente;
import br.edu.ifsp.dominio.politica.HorarioFuncionamento;
import br.edu.ifsp.dominio.politica.PoliticaClinica;
import br.edu.ifsp.dominio.politica.PoliticaDeFaltas;
import br.edu.ifsp.dominio.procedimento.Procedimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@Tag("UnitTest")
@Tag("Functional")
@DisplayName("Funcional - Objetos de valor e invariantes")
class ObjetosDeValorFuncionalTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 7); // quarta

    private PeriodoAtendimento p(int hIni, int mIni, int hFim, int mFim) {
        return new PeriodoAtendimento(DIA.atTime(hIni, mIni), DIA.atTime(hFim, mFim));
    }

    @Test
    @DisplayName("PeriodoAtendimento: intervalos que apenas se tocam não se sobrepõem (e vice-versa)")
    void periodosAdjacentes() {
        assertFalse(p(9, 0, 10, 0).sobrepoe(p(10, 0, 11, 0)));
        assertFalse(p(10, 0, 11, 0).sobrepoe(p(9, 0, 10, 0)));
    }

    @Test
    @DisplayName("PeriodoAtendimento: sobreposição é simétrica")
    void sobreposicaoSimetrica() {
        PeriodoAtendimento a = p(9, 0, 10, 30);
        PeriodoAtendimento b = p(10, 0, 11, 0);
        assertTrue(a.sobrepoe(b));
        assertTrue(b.sobrepoe(a));
    }

    @Test
    @DisplayName("PeriodoAtendimento: um intervalo se sobrepõe a si mesmo")
    void sobrepoeASiMesmo() {
        assertTrue(p(9, 0, 10, 0).sobrepoe(p(9, 0, 10, 0)));
    }

    @Test
    @DisplayName("PeriodoAtendimento: valores nulos são rejeitados com erro de domínio")
    void periodoComNulos() {
        assertThrows(PeriodoInvalidoException.class, () -> new PeriodoAtendimento(null, DIA.atTime(10, 0)));
        assertThrows(PeriodoInvalidoException.class, () -> new PeriodoAtendimento(DIA.atTime(10, 0), null));
    }

    @Test
    @DisplayName("PeriodoAtendimento: mesmoDia() distingue períodos que viram o dia")
    void mesmoDia() {
        assertTrue(p(9, 0, 10, 0).mesmoDia());
        assertFalse(new PeriodoAtendimento(DIA.atTime(23, 0), DIA.plusDays(1).atTime(1, 0)).mesmoDia());
    }

    @Test
    @DisplayName("HorarioFuncionamento: não aceita lista de dias vazia nem fechamento <= abertura")
    void horarioInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(Set.of(), LocalTime.of(8, 0), LocalTime.of(18, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(Set.of(DayOfWeek.MONDAY), LocalTime.of(18, 0), LocalTime.of(18, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioFuncionamento(Set.of(DayOfWeek.MONDAY), LocalTime.of(18, 0), LocalTime.of(8, 0)));
    }

    @Test
    @DisplayName("HorarioFuncionamento configurável: clínica que abre aos sábados")
    void clinicaQueAbreAosSabados() {
        HorarioFuncionamento sabado = new HorarioFuncionamento(
                Set.of(DayOfWeek.SATURDAY), LocalTime.of(9, 0), LocalTime.of(12, 0));
        LocalDate sab = LocalDate.of(2026, 10, 10);

        assertTrue(sabado.comporta(new PeriodoAtendimento(sab.atTime(9, 0), sab.atTime(12, 0))));
        assertFalse(sabado.comporta(new PeriodoAtendimento(sab.atTime(11, 30), sab.atTime(12, 30))));
        assertFalse(sabado.funcionaEm(LocalDate.of(2026, 10, 7)));
    }

    @Test
    @DisplayName("PoliticaDeFaltas: limite < 1 e validade nula/zero/negativa são rejeitados")
    void politicaDeFaltasInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new PoliticaDeFaltas(0, Period.ofMonths(6)));
        assertThrows(IllegalArgumentException.class, () -> new PoliticaDeFaltas(3, null));
        assertThrows(IllegalArgumentException.class, () -> new PoliticaDeFaltas(3, Period.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new PoliticaDeFaltas(3, Period.ofDays(-1)));
    }

    @Test
    @DisplayName("PoliticaDeFaltas: validade de 1 dia, limites da expiração")
    void validadeDeUmDia() {
        PoliticaDeFaltas politica = new PoliticaDeFaltas(1, Period.ofDays(1));
        LocalDateTime falta = LocalDateTime.of(2026, 10, 1, 12, 0);
        Falta f = new Falta(1L, falta);

        assertTrue(politica.estaVigente(f, falta.plusDays(1)));
        assertFalse(politica.estaVigente(f, falta.plusDays(1).plusSeconds(1)));
    }

    @Test
    @DisplayName("Paciente: nome obrigatório (nulo, vazio ou só espaços)")
    void pacienteExigeNome() {
        assertThrows(IllegalArgumentException.class, () -> Paciente.novo(null));
        assertThrows(IllegalArgumentException.class, () -> Paciente.novo(""));
        assertThrows(IllegalArgumentException.class, () -> Paciente.novo("   "));
    }

    @Test
    @DisplayName("Paciente: nome é aparado e novo paciente não tem faltas nem está bloqueado")
    void pacienteNovo() {
        Paciente p = Paciente.novo("  Ana  ");
        PoliticaDeFaltas politica = PoliticaClinica.padrao().faltas();

        assertEquals("Ana", p.getNome());
        assertEquals(0, p.faltasVigentes(LocalDateTime.now(), politica));
        assertFalse(p.estaBloqueado(LocalDateTime.now(), politica));
    }

    @Test
    @DisplayName("Paciente: lista de faltas exposta é imutável")
    void faltasImutaveis() {
        Paciente p = Paciente.novo("Ana");
        assertThrows(UnsupportedOperationException.class,
                () -> p.getFaltas().add(new Falta(1L, LocalDateTime.now())));
    }

    @Test
    @DisplayName("Paciente: id só pode ser definido uma vez")
    void idDefinidoUmaVez() {
        Paciente p = Paciente.novo("Ana");
        p.definirId(1L);
        assertThrows(IllegalStateException.class, () -> p.definirId(2L));
    }

    @Test
    @DisplayName("Procedimento: nome obrigatório")
    void procedimentoExigeNome() {
        assertThrows(IllegalArgumentException.class, () -> new Procedimento(null, " "));
        assertThrows(IllegalArgumentException.class, () -> new Procedimento(null, null));
    }

    @Test
    @DisplayName("Política padrão documenta as premissas adotadas (seg-sex 08-18h, 24h, 3 faltas, 6 meses)")
    void politicaPadrao() {
        PoliticaClinica pol = PoliticaClinica.padrao();

        assertEquals(Duration.ofHours(24), pol.prazoMinimoCancelamento());
        assertEquals(3, pol.faltas().limite());
        assertEquals(Period.ofMonths(6), pol.faltas().validade());
        assertEquals(LocalTime.of(8, 0), pol.horario().abertura());
        assertEquals(LocalTime.of(18, 0), pol.horario().fechamento());
        assertEquals(5, pol.horario().dias().size());
    }
}
