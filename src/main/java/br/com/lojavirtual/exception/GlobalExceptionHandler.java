package br.com.lojavirtual.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura os erros de Regra de Negócio (ex: E-mail já em uso)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PadraoErro> handleRuntimeException(RuntimeException ex) {
        PadraoErro erro = new PadraoErro(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}