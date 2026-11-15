package br.com.lojavirtual.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    // Erros de validação (@Valid): devolve a primeira mensagem, ex.: "A senha deve ter no mínimo 6 caracteres"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PadraoErro> handleValidacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        PadraoErro erro = new PadraoErro(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
}
