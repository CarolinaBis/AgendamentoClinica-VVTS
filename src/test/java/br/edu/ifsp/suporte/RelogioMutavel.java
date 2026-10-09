package br.edu.ifsp.suporte;

import java.time.*;

public class RelogioMutavel extends Clock {

    private LocalDateTime agora;

    public RelogioMutavel(LocalDateTime inicial) {
        this.agora = inicial;
    }

    public void definir(LocalDateTime novoInstante) {
        this.agora = novoInstante;
    }

    public LocalDateTime agora() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return agora.toInstant(ZoneOffset.UTC);
    }
}
