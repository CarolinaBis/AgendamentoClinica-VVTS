package br.edu.ifsp.integracao;

import br.edu.ifsp.ui.pages.AuthenticationPage;
import br.edu.ifsp.ui.pages.ClinicDashboardPage;
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

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:sqlite:target/paciente-ui-tests.db?foreign_keys=on")
@Tag("UiTest")
@Tag("IntegrationTest")
class PacienteUiIntegrationTest {

    private final Faker faker = new Faker();

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private AuthenticationPage authenticationPage;
    private ClinicDashboardPage clinicDashboardPage;

    @BeforeEach
    void abrirNavegadorEEntrar() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1280,900");
        driver = new ChromeDriver(options);
        authenticationPage = new AuthenticationPage(driver, "http://localhost:" + port + "/");
        authenticationPage.open();

        String email = faker.internet().emailAddress();
        String password = faker.internet().password();
        authenticationPage.register(
                faker.name().firstName(), faker.name().lastName(), email, password);
        authenticationPage.login(email, password);
        clinicDashboardPage = new ClinicDashboardPage(driver);
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void deveCadastrarPacienteEExibirIdGerado() {
        String nome = faker.name().fullName();

        clinicDashboardPage.registerPatient(nome);

        String aviso = clinicDashboardPage.getNotice();
        assertTrue(aviso.matches("Paciente cadastrado: id \\d+\\."));
        String id = aviso.substring("Paciente cadastrado: id ".length(), aviso.length() - 1);
        String resultado = clinicDashboardPage.getPatientResult();
        assertTrue(resultado.contains("\"id\": " + id));
        assertTrue(resultado.contains("\"nome\": \"" + nome + "\""));
    }
}
