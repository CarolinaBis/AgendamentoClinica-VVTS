package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AuthenticationPage {

    private static final By REGISTRATION_FORM = By.id("form-registrar");
    private static final By LOGIN_FORM = By.id("form-entrar");
    private static final By NOTICE = By.id("aviso");
    private static final By APPLICATION = By.id("tela-app");
    private static final By USER_EMAIL = By.id("usuario-email");
    private static final By USER_INFO = By.id("usuario");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    public AuthenticationPage(WebDriver driver, String baseUrl) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        this.baseUrl = baseUrl;
    }

    public void open() {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(REGISTRATION_FORM));
    }

    public boolean isLoginFormVisible() {
        return driver.findElement(LOGIN_FORM).isDisplayed();
    }

    public boolean isApplicationVisible() {
        return driver.findElement(APPLICATION).isDisplayed();
    }

    public boolean isUserInfoVisible() {
        return driver.findElement(USER_INFO).isDisplayed();
    }

    public boolean hasSessionToken() {
        Object token = ((JavascriptExecutor) driver).executeScript(
                "return sessionStorage.getItem('token');");
        return token != null;
    }

    public String getNotice() {
        return driver.findElement(NOTICE).getText();
    }

    public void register(String name, String lastname, String email, String password) {
        submitRegistration(name, lastname, email, password);
        wait.until(ExpectedConditions.textToBe(
                NOTICE, "Usuário criado. Agora entre com o seu e-mail e senha."));
    }

    public void submitRegistration(String name, String lastname, String email, String password) {
        driver.findElement(By.cssSelector("#form-registrar [name='name']")).sendKeys(name);
        driver.findElement(By.cssSelector("#form-registrar [name='lastname']")).sendKeys(lastname);
        driver.findElement(By.cssSelector("#form-registrar [name='email']")).sendKeys(email);
        driver.findElement(By.cssSelector("#form-registrar [name='password']")).sendKeys(password);
        driver.findElement(By.id("btn-registrar")).click();
    }

    public boolean isRegistrationEmailInvalid() {
        return !driver.findElements(
                By.cssSelector("#form-registrar input[name='email']:invalid")).isEmpty();
    }

    public int getInvalidRegistrationFieldCount() {
        return driver.findElements(By.cssSelector("#form-registrar :invalid")).size();
    }

    public void login(String email, String password) {
        submitLogin(email, password);

        wait.until(ExpectedConditions.visibilityOfElementLocated(APPLICATION));
        wait.until(ExpectedConditions.textToBe(USER_EMAIL, email));
    }

    public void submitLogin(String email, String password) {
        driver.findElement(By.cssSelector("#form-entrar [name='username']")).sendKeys(email);
        driver.findElement(By.cssSelector("#form-entrar [name='password']")).sendKeys(password);
        driver.findElement(By.id("btn-entrar")).click();
    }

    public void waitForNotice(String notice) {
        wait.until(ExpectedConditions.textToBe(NOTICE, notice));
    }

    public void logout() {
        driver.findElement(By.id("sair")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_FORM));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(APPLICATION));
        wait.until(ExpectedConditions.textToBe(NOTICE, "Você saiu."));
    }
}
