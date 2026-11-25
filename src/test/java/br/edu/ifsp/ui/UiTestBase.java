package br.edu.ifsp.ui;

import br.edu.ifsp.ui.pages.AppPage;
import br.edu.ifsp.ui.pages.LoginPage;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:sqlite:target/ui-test.db?foreign_keys=on")
public abstract class UiTestBase {

    protected static final Faker faker = new Faker();
    protected static final String SENHA = "Senha@123";

    @LocalServerPort
    private int porta;

    @Autowired
    private JdbcTemplate jdbc;

    protected ChromeDriver driver;

    @BeforeEach
    void abrirNavegador() {
        limparBanco();
        ChromeOptions opcoes = new ChromeOptions();
        if (!Boolean.getBoolean("ui.visivel")) {
            opcoes.addArguments("--headless=new");
        }
        opcoes.addArguments("--window-size=1280,900");
        driver = new ChromeDriver(opcoes);
    }

    @AfterEach
    void fecharNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected LoginPage abrirSistema() {
        driver.get("http://localhost:" + porta + "/");
        return new LoginPage(driver);
    }

    protected AppPage entrarComNovoUsuario() {
        String email = faker.internet().emailAddress();
        LoginPage login = abrirSistema();
        login.registrar(faker.name().firstName(), faker.name().lastName(), email, SENHA);
        login.aguardarAviso("Usuário criado");
        login.entrar(email, SENHA);
        AppPage app = new AppPage(driver);
        app.aguardarCarregar();
        return app;
    }

    private void limparBanco() {
        jdbc.update("DELETE FROM agendamento_procedimento");
        jdbc.update("DELETE FROM falta");
        jdbc.update("DELETE FROM agendamento");
        jdbc.update("DELETE FROM procedimento");
        jdbc.update("DELETE FROM paciente");
        jdbc.update("DELETE FROM app_user");
    }
}