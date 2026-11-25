package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AppPage extends BasePage {

    public AppPage(WebDriver driver) {
        super(driver);
    }

    public void aguardarCarregar() {
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("tela-app")));
    }

    public boolean estaVisivel() {
        return driver.findElement(By.id("tela-app")).isDisplayed();
    }

    public String emailDoUsuarioLogado() {
        return driver.findElement(By.id("usuario-email")).getText();
    }

    public LoginPage sair() {
        driver.findElement(By.id("sair")).click();
        return new LoginPage(driver);
    }
}
