package br.edu.ifsp.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppPage extends BasePage {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

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

    public long cadastrarPaciente(String nome) {
        preencher(By.cssSelector("#form-paciente input[name='nome']"), nome);
        driver.findElement(By.id("btn-cadastrar-paciente")).click();
        aguardarResultado(nome);
        return idInformadoNoAviso();
    }

    public long cadastrarProcedimento(String nome) {
        preencher(By.cssSelector("#form-procedimento input[name='nome']"), nome);
        driver.findElement(By.id("btn-cadastrar-procedimento")).click();
        aguardarResultado(nome);
        return idInformadoNoAviso();
    }

    public void listarProcedimentos() {
        driver.findElement(By.id("listar-procedimentos")).click();
    }

    public List<String> nomesDosProcedimentosListados() {
        return driver.findElements(By.cssSelector("#resultado table tr td:nth-child(2)"))
                .stream().map(WebElement::getText).toList();
    }

    public void agendar(long pacienteId, long profissionalId, LocalDateTime inicio, LocalDateTime fim,
                        long procedimentoId) {
        preencher(By.cssSelector("#form-agendar input[name='pacienteId']"), String.valueOf(pacienteId));
        preencher(By.cssSelector("#form-agendar input[name='profissionalId']"), String.valueOf(profissionalId));
        definirValorPorScript(By.cssSelector("#form-agendar input[name='inicio']"), inicio.format(DATA_HORA));
        definirValorPorScript(By.cssSelector("#form-agendar input[name='fim']"), fim.format(DATA_HORA));
        preencher(By.cssSelector("#form-agendar input[name='procedimentoIds']"), String.valueOf(procedimentoId));
        driver.findElement(By.id("btn-agendar")).click();
    }

    public String statusDoPrimeiroAgendamentoListado() {
        return driver.findElement(By.cssSelector("#resultado table tr:nth-child(2) td:nth-child(6)")).getText();
    }

    public void verFaltas() {
        driver.findElement(By.id("ver-faltas")).click();
    }

    public void cancelarAgendamento() {
        driver.findElement(By.id("btn-cancelar")).click();
    }

    private void aguardarResultado(String trecho) {
        espera.until(ExpectedConditions.textToBePresentInElementLocated(By.id("resultado"), trecho));
    }

    private long idInformadoNoAviso() {
        Matcher id = Pattern.compile("id (\\d+)").matcher(textoDoAviso());
        id.find();
        return Long.parseLong(id.group(1));
    }

}
