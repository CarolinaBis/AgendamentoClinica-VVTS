package br.edu.ifsp.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AgendarRequest(Long pacienteId, Long profissionalId, LocalDateTime inicio, LocalDateTime fim,
                             List<Long> procedimentoIds) {
}
