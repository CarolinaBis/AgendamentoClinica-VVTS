package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class ClinicDashboardPage {

    private static final By NOTICE = By.id("aviso");
    private static final By PATIENT_RESULT = By.cssSelector("#resultado pre");
    private static final By RESULT_TABLE = By.cssSelector("#resultado table");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ClinicDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public long registerPatient(String name) {
        driver.findElement(By.cssSelector("#form-paciente [name='nome']")).sendKeys(name);
        driver.findElement(By.id("btn-cadastrar-paciente")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Paciente cadastrado: id \\d+\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PATIENT_RESULT));
        return generatedId("Paciente cadastrado: id ");
    }

    public long registerProcedure(String name) {
        driver.findElement(By.cssSelector("#form-procedimento [name='nome']")).sendKeys(name);
        driver.findElement(By.id("btn-cadastrar-procedimento")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Procedimento cadastrado: id \\d+\\.")));
        return generatedId("Procedimento cadastrado: id ");
    }

    public void listProcedures() {
        driver.findElement(By.id("listar-procedimentos")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("\\d+ procedimento\\(s\\)\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_TABLE));
    }

    public void scheduleAppointment(long patientId, long professionalId, String start, String end,
                                    List<Long> procedureIds) {
        driver.findElement(By.cssSelector("#form-agendar [name='pacienteId']"))
                .sendKeys(Long.toString(patientId));
        driver.findElement(By.cssSelector("#form-agendar [name='profissionalId']"))
                .sendKeys(Long.toString(professionalId));
        setInputValue("#form-agendar [name='inicio']", start);
        setInputValue("#form-agendar [name='fim']", end);
        driver.findElement(By.cssSelector("#form-agendar [name='procedimentoIds']"))
                .sendKeys(String.join(",", procedureIds.stream().map(String::valueOf).toList()));
        driver.findElement(By.id("btn-agendar")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Consulta agendada: id \\d+ \\(.+\\)\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_TABLE));
    }

    public void consultProfessionalAgenda(long professionalId, String date) {
        driver.findElement(By.cssSelector("#form-agenda [name='profissionalId']"))
                .sendKeys(Long.toString(professionalId));
        setInputValue("#form-agenda [name='data']", date);
        driver.findElement(By.id("btn-consultar-agenda")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Agenda de " + Pattern.quote(date)
                        + ": \\d+ agendamento\\(s\\)\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_TABLE));
    }

    public void consultPatientHistory(long patientId, String status, String fromDate, String toDate) {
        driver.findElement(By.cssSelector("#form-historico [name='pacienteId']"))
                .sendKeys(Long.toString(patientId));
        new Select(driver.findElement(By.cssSelector("#form-historico [name='status']")))
                .selectByVisibleText(status);
        setInputValue("#form-historico [name='de']", fromDate);
        setInputValue("#form-historico [name='ate']", toDate);
        driver.findElement(By.id("btn-ver-historico")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("\\d+ agendamento\\(s\\) no histórico\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(RESULT_TABLE));
    }

    private void setInputValue(String selector, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
                        + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                driver.findElement(By.cssSelector(selector)), value);
    }

    public String getNotice() {
        return driver.findElement(NOTICE).getText();
    }

    public String getPatientResult() {
        return driver.findElement(PATIENT_RESULT).getText();
    }

    public String getProcedureList() {
        return driver.findElement(RESULT_TABLE).getText();
    }

    public String getResultTable() {
        return driver.findElement(RESULT_TABLE).getText();
    }

    private long generatedId(String prefix) {
        String notice = getNotice();
        return Long.parseLong(notice.substring(prefix.length(), notice.length() - 1));
    }
}
