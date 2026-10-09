package br.edu.ifsp.dominio.agendamento;

public enum StatusAgendamento {
    CONFIRMADO("Confirmado", "confirmado"),
    CANCELADO("Cancelado", "cancelado"),
    REALIZADO("Realizado", "realizado"),
    NAO_COMPARECEU("Não Compareceu", "com falta registrada");

    private final String rotulo;
    private final String adjetivo;

    StatusAgendamento(String rotulo, String adjetivo) {
        this.rotulo = rotulo;
        this.adjetivo = adjetivo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getAdjetivo() {
        return adjetivo;
    }
}
