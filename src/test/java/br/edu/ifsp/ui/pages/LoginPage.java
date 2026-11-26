package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void registrar(String nome, String sobrenome, String email, String senha) {
        preencher(By.cssSelector("#form-registrar input[name='name']"), nome);
        preencher(By.cssSelector("#form-registrar input[name='lastname']"), sobrenome);
        preencher(By.cssSelector("#form-registrar input[name='email']"), email);
        preencher(By.cssSelector("#form-registrar input[name='password']"), senha);
        driver.findElement(By.id("btn-registrar")).click();
    }

    public void entrar(String email, String senha) {
        preencher(By.cssSelector("#form-entrar input[name='username']"), email);
        preencher(By.cssSelector("#form-entrar input[name='password']"), senha);
        driver.findElement(By.id("btn-entrar")).click();
    }

    public boolean estaVisivel() {
        return driver.findElement(By.id("tela-login")).isDisplayed();
    }

    public boolean formularioDeRegistroEstaValido() {
        return (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "return document.getElementById('form-registrar').checkValidity();");
    }

    public org.openqa.selenium.Rectangle areaDoCardEntrar() {
        return driver.findElement(By.id("form-entrar")).getRect();
    }

    public org.openqa.selenium.Rectangle areaDoCardRegistrar() {
        return driver.findElement(By.id("form-registrar")).getRect();
    }
}