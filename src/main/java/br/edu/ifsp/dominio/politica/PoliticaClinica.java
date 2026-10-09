package br.edu.ifsp.dominio.politica;

import java.time.Duration;
import java.time.Period;

public record PoliticaClinica(HorarioFuncionamento horario,
                              Duration prazoMinimoCancelamento,
                              PoliticaDeFaltas faltas) {

    public static PoliticaClinica padrao() {
        return new PoliticaClinica(HorarioFuncionamento.padrao(), Duration.ofHours(24),
                new PoliticaDeFaltas(3, Period.ofMonths(6)));
    }
}
