package br.com.lojavirtual.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusPedidoDTO {

    @NotBlank(message = "O status é obrigatório")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
