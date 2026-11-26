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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:sqlite:target/historico-ui-tests.db?foreign_keys=on")
@Tag("UiTest")
@Tag("IntegrationTest")
class HistoricoPacienteUiIntegrationTest {

    private static final DateTimeFormatter DATE_TIME_INPUT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter DATE_TIME_DISPLAY =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_INPUT = DateTimeFormatter.ISO_LOCAL_DATE;

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
    void deveConsultarHistoricoDoPacienteComFiltros() {
        long pacienteId = clinicDashboardPage.registerPatient(faker.name().fullName());
        long procedimentoId = clinicDashboardPage.registerProcedure(faker.commerce().productName());
        long profissionalId = faker.number().numberBetween(1000, 9999);
        LocalDate dia = proximoDiaUtil();
        LocalDateTime inicio = dia.atTime(10, 0);

        clinicDashboardPage.scheduleAppointment(pacienteId, profissionalId,
                inicio.format(DATE_TIME_INPUT), inicio.plusMinutes(30).format(DATE_TIME_INPUT),
                List.of(procedimentoId));
        clinicDashboardPage.consultPatientHistory(pacienteId, "CONFIRMADO",
                dia.format(DATE_INPUT), dia.format(DATE_INPUT));

        assertEquals("1 agendamento(s) no histórico.", clinicDashboardPage.getNotice());
        String resultado = clinicDashboardPage.getResultTable();
        assertTrue(resultado.contains(Long.toString(pacienteId)));
        assertTrue(resultado.contains(Long.toString(profissionalId)));
        assertTrue(resultado.contains(inicio.format(DATE_TIME_DISPLAY)));
        assertTrue(resultado.contains("Confirmado"));
        assertTrue(resultado.contains(Long.toString(procedimentoId)));
    }

    private LocalDate proximoDiaUtil() {
        LocalDate dia = LocalDate.now().plusDays(1);
        while (dia.getDayOfWeek() == DayOfWeek.SATURDAY
                || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.plusDays(1);
        }
        return dia;
    }
}
