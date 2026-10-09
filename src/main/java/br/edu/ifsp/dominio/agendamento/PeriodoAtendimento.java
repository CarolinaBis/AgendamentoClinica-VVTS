package br.edu.ifsp.dominio.agendamento;

import br.edu.ifsp.dominio.excecao.PeriodoInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodoAtendimento(LocalDateTime inicio, LocalDateTime fim) {

    public PeriodoAtendimento {
        if (inicio == null || fim == null) {
            throw new PeriodoInvalidoException("Início e fim do período são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new PeriodoInvalidoException("O fim do período deve ser posterior ao início.");
        }
    }

    public boolean sobrepoe(PeriodoAtendimento outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }

    public LocalDate data() {
        return inicio.toLocalDate();
    }

    public boolean mesmoDia() {
        return fim.toLocalDate().equals(inicio.toLocalDate());
    }
}
