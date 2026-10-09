package br.edu.ifsp.dominio.politica;

import br.edu.ifsp.dominio.paciente.Falta;

import java.time.LocalDateTime;
import java.time.Period;

public record PoliticaDeFaltas(int limite, Period validade) {

    public PoliticaDeFaltas {
        if (limite < 1) {
            throw new IllegalArgumentException("O limite de faltas deve ser de pelo menos 1.");
        }
        if (validade == null || validade.isNegative() || validade.isZero()) {
            throw new IllegalArgumentException("A validade da falta deve ser positiva.");
        }
    }

    public boolean estaVigente(Falta falta, LocalDateTime agora) {
        return !agora.isAfter(falta.dataReferencia().plus(validade));
    }
}
