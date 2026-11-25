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

    private LocalDateTime proximaSegundaAs(int hora) {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(hora, 0);
    }
}