package br.edu.ifsp.dominio.agendamento;

import java.util.Objects;

public class ProcedimentoAgendado {

    private final Long procedimentoId;
    private boolean executado;

    private ProcedimentoAgendado(Long procedimentoId, boolean executado) {
        this.procedimentoId = Objects.requireNonNull(procedimentoId, "procedimentoId");
        this.executado = executado;
    }

    public static ProcedimentoAgendado pendente(Long procedimentoId) {
        return new ProcedimentoAgendado(procedimentoId, false);
    }

    public static ProcedimentoAgendado reconstituir(Long procedimentoId, boolean executado) {
        return new ProcedimentoAgendado(procedimentoId, executado);
    }

    void marcarExecutado() {
        this.executado = true;
    }

    public Long getProcedimentoId() {
        return procedimentoId;
    }

    public boolean isExecutado() {
        return executado;
    }
}
