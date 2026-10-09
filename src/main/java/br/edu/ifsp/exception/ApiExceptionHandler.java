package br.edu.ifsp.exception;

import br.edu.ifsp.dominio.excecao.ConflitoDeAgendaException;
import br.edu.ifsp.dominio.excecao.DataHoraNoPassadoException;
import br.edu.ifsp.dominio.excecao.DominioException;
import br.edu.ifsp.dominio.excecao.ForaDoHorarioDeFuncionamentoException;
import br.edu.ifsp.dominio.excecao.HorarioNaoAtingidoException;
import br.edu.ifsp.dominio.excecao.PacienteBloqueadoException;
import br.edu.ifsp.dominio.excecao.ProcedimentoDuplicadoException;
import br.edu.ifsp.dominio.excecao.ProcedimentoNaoEncontradoException;
import br.edu.ifsp.dominio.excecao.ProcedimentosPendentesException;
import br.edu.ifsp.dominio.excecao.RecursoNaoEncontradoException;
import br.edu.ifsp.dominio.excecao.StatusInvalidoException;
import br.edu.ifsp.dominio.excecao.UltimoProcedimentoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        return resposta(BAD_REQUEST, e);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return resposta(BAD_REQUEST, e);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return resposta(FORBIDDEN, e);
    }

    @ExceptionHandler(value = EntityAlreadyExistsException.class)
    public ResponseEntity<?> handleEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        return resposta(CONFLICT, e);
    }

    @ExceptionHandler(value = DominioException.class)
    public ResponseEntity<?> handleDominioException(DominioException e) {
        return resposta(statusDe(e), e);
    }

    private static ResponseEntity<ApiException> resposta(HttpStatus status, Throwable erro) {
        return new ResponseEntity<>(ApiException.of(status, erro), status);
    }

    private static HttpStatus statusDe(DominioException e) {
        if (e instanceof RecursoNaoEncontradoException || e instanceof ProcedimentoNaoEncontradoException) {
            return NOT_FOUND;
        }
        if (e instanceof ConflitoDeAgendaException || e instanceof StatusInvalidoException
                || e instanceof HorarioNaoAtingidoException || e instanceof UltimoProcedimentoException
                || e instanceof ProcedimentosPendentesException || e instanceof ProcedimentoDuplicadoException) {
            return CONFLICT;
        }
        if (e instanceof PacienteBloqueadoException || e instanceof ForaDoHorarioDeFuncionamentoException
                || e instanceof DataHoraNoPassadoException) {
            return UNPROCESSABLE_ENTITY;
        }
        return BAD_REQUEST;
    }
}
