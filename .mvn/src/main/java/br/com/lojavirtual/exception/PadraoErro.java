package br.com.lojavirtual.exception;

import java.time.LocalDateTime;

public class PadraoErro {
    private final LocalDateTime timestamp;
    private final Integer status;
    private final String error;
    private final String mensage;

    public PadraoErro(LocalDateTime timestamp, Integer status, String error, String mensage) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensage = mensage;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public Integer getStatus() { return status; }
    public String getError() { return error; }
    public String getMensage() { return mensage; }
}
