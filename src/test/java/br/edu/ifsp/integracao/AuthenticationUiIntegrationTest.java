package br.edu.ifsp.integracao;

import br.edu.ifsp.ui.pages.AuthenticationPage;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:sqlite:target/ui-tests.db?foreign_keys=on")
@Tag("UiTest")
@Tag("IntegrationTest")
class AuthenticationUiIntegrationTest {

    private final Faker faker = new Faker();

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private AuthenticationPage authenticationPage;

    @BeforeEach
    void abrirNavegador() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,900");
        driver = new ChromeDriver(options);
        authenticationPage = new AuthenticationPage(driver, "http://localhost:" + port + "/");
        authenticationPage.open();
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void deveRegistrarUsuarioEEntrarPelaInterface() {
        String nome = faker.name().firstName();
        String sobrenome = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String senha = faker.internet().password();

        assertTrue(authenticationPage.isLoginFormVisible());
        assertFalse(authenticationPage.isApplicationVisible());

        authenticationPage.register(nome, sobrenome, email, senha);
        assertEquals("Usuário criado. Agora entre com o seu e-mail e senha.",
                authenticationPage.getNotice());

        authenticationPage.login(email, senha);

        assertTrue(authenticationPage.isApplicationVisible());
        assertFalse(authenticationPage.isLoginFormVisible());
        assertEquals("Bem-vindo(a), " + email + ".", authenticationPage.getNotice());
    }

    @Test
    void deveExibirErroAoEntrarComCredenciaisInvalidas() {
        authenticationPage.submitLogin(
                faker.internet().emailAddress(), faker.internet().password());
        authenticationPage.waitForNotice("E-mail ou senha inválidos.");

        assertTrue(authenticationPage.isLoginFormVisible());
        assertFalse(authenticationPage.isApplicationVisible());
        assertEquals("E-mail ou senha inválidos.", authenticationPage.getNotice());
    }

    @Test
    void deveBloquearCadastroComEmailEmFormatoInvalido() {
        String avisoInicial = authenticationPage.getNotice();
        authenticationPage.submitRegistration(
                faker.name().firstName(), faker.name().lastName(),
                "email-invalido", faker.internet().password());

        assertTrue(authenticationPage.isRegistrationEmailInvalid());
        assertTrue(authenticationPage.isLoginFormVisible());
        assertFalse(authenticationPage.isApplicationVisible());
        assertEquals(avisoInicial, authenticationPage.getNotice());
    }
}
