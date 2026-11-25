package br.edu.ifsp.ui;

import br.edu.ifsp.ui.pages.LoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UiTest")
class LoginUiTest extends UiTestBase {

    @Test
    @DisplayName("Registrar usuário com dados válidos mostra a mensagem de sucesso")
    void registrarUsuarioComDadosValidosMostraMensagemDeSucesso() {
        LoginPage login = abrirSistema();

        login.registrar(faker.name().firstName(), faker.name().lastName(),
                faker.internet().emailAddress(), SENHA);

        login.aguardarAviso("Usuário criado. Agora entre com o seu e-mail e senha.");
        assertThat(login.avisoEhDeErro()).isFalse();
    }

    @Test
    @DisplayName("Registrar com campos vazios é bloqueado pelo navegador e não envia nada")
    void registrarComCamposVaziosEBloqueado() {
        LoginPage login = abrirSistema();

        login.registrar("", "", "", "");

        assertThat(login.formularioDeRegistroEstaValido()).isFalse();
        assertThat(login.textoDoAviso()).isEqualTo("Entre ou registre um usuário para começar.");
    }


}