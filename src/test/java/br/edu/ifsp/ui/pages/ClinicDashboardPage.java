package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.regex.Pattern;

public class ClinicDashboardPage {

    private static final By NOTICE = By.id("aviso");
    private static final By PATIENT_RESULT = By.cssSelector("#resultado pre");
    private static final By PROCEDURES_TABLE = By.cssSelector("#resultado table");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ClinicDashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void registerPatient(String name) {
        driver.findElement(By.cssSelector("#form-paciente [name='nome']")).sendKeys(name);
        driver.findElement(By.id("btn-cadastrar-paciente")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Paciente cadastrado: id \\d+\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PATIENT_RESULT));
    }

    public void registerProcedure(String name) {
        driver.findElement(By.cssSelector("#form-procedimento [name='nome']")).sendKeys(name);
        driver.findElement(By.id("btn-cadastrar-procedimento")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("Procedimento cadastrado: id \\d+\\.")));
    }

    public void listProcedures() {
        driver.findElement(By.id("listar-procedimentos")).click();

        wait.until(ExpectedConditions.textMatches(
                NOTICE, Pattern.compile("\\d+ procedimento\\(s\\)\\.")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(PROCEDURES_TABLE));
    }

    public String getNotice() {
        return driver.findElement(NOTICE).getText();
    }

    public String getPatientResult() {
        return driver.findElement(PATIENT_RESULT).getText();
    }

    public String getProcedureList() {
        return driver.findElement(PROCEDURES_TABLE).getText();
    }
}
