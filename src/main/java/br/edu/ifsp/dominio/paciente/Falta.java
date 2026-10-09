package br.edu.ifsp.dominio.paciente;

import java.time.LocalDateTime;

public record Falta(Long agendamentoId, LocalDateTime dataReferencia) {
}
