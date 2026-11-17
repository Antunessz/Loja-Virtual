package br.com.lojavirtual.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Regras de negócio (e-mail em uso, estoque insuficiente, credenciais inválidas...)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<PadraoErro> handleNegocio(RegraNegocioException ex) {
        return resposta(ex.getStatus(), ex.getMessage());
    }

    // Validações @Valid dos DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PadraoErro> handleValidacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        return resposta(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PadraoErro> handleCorpoInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    // Qualquer outro erro é um BUG: registra no log e devolve 500 (sem expor detalhes internos)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<PadraoErro> handleInesperado(RuntimeException ex) {
        log.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<PadraoErro> resposta(HttpStatus status, String msg) {
        PadraoErro erro = new PadraoErro(LocalDateTime.now(), status.value(), status.getReasonPhrase(), msg);
        return ResponseEntity.status(status).body(erro);
    }
}
