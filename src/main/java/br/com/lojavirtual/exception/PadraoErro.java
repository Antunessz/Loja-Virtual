package br.com.lojavirtual.exceprion;

import java.time.LocalDateTime;

public class PadraoErro {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String mensage;

    public PadraoErro(LocalDateTime timestamp, Integer status, String error, String mensage){
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.mensage = mensage;

    }
}
