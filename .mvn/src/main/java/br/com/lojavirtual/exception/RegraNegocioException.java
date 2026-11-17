package br.com.lojavirtual.exception;

import org.springframework.http.HttpStatus;

// Erro esperado de regra de negócio (devolve a mensagem ao cliente com o status certo)
public class RegraNegocioException extends RuntimeException {

    private final HttpStatus status;

    public RegraNegocioException(String mensagem) {
        this(mensagem, HttpStatus.BAD_REQUEST);
    }

    public RegraNegocioException(String mensagem, HttpStatus status) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
