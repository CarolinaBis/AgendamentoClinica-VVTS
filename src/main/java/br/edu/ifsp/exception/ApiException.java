package br.edu.ifsp.exception;

import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Corpo padrão das respostas de erro (mesmos campos do projeto base). Record: sem Lombok.
 */
public record ApiException(String message, HttpStatus status, ZonedDateTime timestamp, String developerMessage) {

    public static ApiException of(HttpStatus status, Throwable erro) {
        return new ApiException(erro.getMessage(), status, ZonedDateTime.now(ZoneId.of("Z")),
                erro.getClass().getName());
    }
}
