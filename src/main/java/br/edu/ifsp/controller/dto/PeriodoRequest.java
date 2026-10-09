package br.edu.ifsp.controller.dto;

import java.time.LocalDateTime;

public record PeriodoRequest(LocalDateTime inicio, LocalDateTime fim) {
}
