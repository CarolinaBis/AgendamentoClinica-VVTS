package br.edu.ifsp.ui;

import br.edu.ifsp.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Rectangle;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
class ResponsividadeUiTest extends UiTestBase {

    @Test
    @DisplayName("Em tela de computador (1280 px), os cards de entrar e registrar ficam lado a lado")
    void emComputadorCardsFicamLadoALado() {
        emularTela(1280, 900);
        LoginPage login = abrirSistema();

        Rectangle entrar = login.areaDoCardEntrar();
        Rectangle registrar = login.areaDoCardRegistrar();

        assertThat(registrar.getY()).isEqualTo(entrar.getY());
        assertThat(registrar.getX()).isGreaterThan(entrar.getX());
        assertThat(login.temRolagemHorizontal()).isFalse();
    }

    @Test
    @DisplayName("Em tela de celular (375 px), os cards ficam empilhados e sem rolagem horizontal")
    void emCelularCardsFicamEmpilhados() {
        emularTela(375, 812);
        LoginPage login = abrirSistema();

        Rectangle entrar = login.areaDoCardEntrar();
        Rectangle registrar = login.areaDoCardRegistrar();

        assertThat(registrar.getY()).isGreaterThan(entrar.getY());
        assertThat(login.temRolagemHorizontal()).isFalse();
    }

    private void emularTela(int largura, int altura) {
        driver.executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
                "width", largura, "height", altura, "deviceScaleFactor", 1, "mobile", largura < 768));
    }
}