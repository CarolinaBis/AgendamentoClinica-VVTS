package br.edu.ifsp.infraestrutura;

import br.edu.ifsp.dominio.politica.PoliticaClinica;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ConfiguracaoDominio {

    @Bean
    public Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    @Bean
    public PoliticaClinica politicaClinica() {
        return PoliticaClinica.padrao();
    }
}
