package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    private static final By AVISO = By.id("aviso");

    protected final WebDriver driver;
    protected final WebDriverWait espera;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.espera = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String textoDoAviso() {
        return driver.findElement(AVISO).getText();
    }

    public void aguardarAviso(String trecho) {
        espera.until(ExpectedConditions.textToBePresentInElementLocated(AVISO, trecho));
    }

    public boolean avisoEhDeErro() {
        String classe = driver.findElement(AVISO).getDomAttribute("class");
        return classe != null && classe.contains("erro");
    }

    public void aguardarErro() {
        espera.until(ExpectedConditions.attributeContains(AVISO, "class", "erro"));
    }

    protected void preencher(By campo, String valor) {
        WebElement elemento = driver.findElement(campo);
        elemento.clear();
        elemento.sendKeys(valor);
    }

    protected void definirValorPorScript(By campo, String valor) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];", driver.findElement(campo), valor);
    }
}
