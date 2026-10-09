package br.edu.ifsp.dominio.politica;

import br.edu.ifsp.dominio.agendamento.PeriodoAtendimento;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record HorarioFuncionamento(Set<DayOfWeek> dias, LocalTime abertura, LocalTime fechamento) {

    public HorarioFuncionamento {
        if (dias == null || dias.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos um dia de funcionamento.");
        }
        if (abertura == null || fechamento == null || !fechamento.isAfter(abertura)) {
            throw new IllegalArgumentException("O fechamento deve ser posterior à abertura.");
        }
        dias = Set.copyOf(dias);
    }

    public static HorarioFuncionamento padrao() {
        return new HorarioFuncionamento(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                LocalTime.of(8, 0), LocalTime.of(18, 0));
    }

    public boolean funcionaEm(LocalDate data) {
        return dias.contains(data.getDayOfWeek());
    }

    public boolean comporta(PeriodoAtendimento periodo) {
        return periodo.mesmoDia()
                && funcionaEm(periodo.data())
                && !periodo.inicio().toLocalTime().isBefore(abertura)
                && !periodo.fim().toLocalTime().isAfter(fechamento);
    }
}
