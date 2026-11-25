package br.edu.ifsp.ui;

import br.edu.ifsp.ui.pages.AppPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
class AplicacaoUiTest extends UiTestBase {

    @Test
    @DisplayName("Cadastrar paciente mostra a confirmação com o id gerado")
    void cadastrarPacienteMostraConfirmacao() {
        AppPage app = entrarComNovoUsuario();

        long id = app.cadastrarPaciente(faker.name().fullName());

        assertThat(id).isPositive();
        assertThat(app.textoDoAviso()).startsWith("Paciente cadastrado: id");
        assertThat(app.avisoEhDeErro()).isFalse();
    }

    @Test
    @DisplayName("Cadastrar procedimentos e listar mostra todos na tabela")
    void cadastrarEListarProcedimentos() {
        AppPage app = entrarComNovoUsuario();
        app.cadastrarProcedimento("Consulta clínica");
        app.cadastrarProcedimento("Exame de sangue");

        app.listarProcedimentos();

        app.aguardarAviso("2 procedimento(s).");
        assertThat(app.nomesDosProcedimentosListados()).containsExactly("Consulta clínica", "Exame de sangue");
    }

    @Test
    @DisplayName("Agendar consulta em horário válido mostra a consulta confirmada")
    void agendarConsultaValidaMostraConfirmada() {
        AppPage app = entrarComNovoUsuario();
        long pacienteId = app.cadastrarPaciente(faker.name().fullName());
        long procedimentoId = app.cadastrarProcedimento("Consulta clínica");
        LocalDateTime inicio = proximaSegundaAs(9);

        app.agendar(pacienteId, 10L, inicio, inicio.plusHours(1), procedimentoId);

        app.aguardarAviso("Consulta agendada");
        assertThat(app.statusDoPrimeiroAgendamentoListado()).isEqualTo("Confirmado");
    }



    private LocalDateTime proximaSegundaAs(int hora) {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(hora, 0);
    }
}