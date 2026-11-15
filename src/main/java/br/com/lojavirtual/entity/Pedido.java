package br.com.lojavirtual.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PEDIDO")
public class Pedido {
    @Transient
    private java.util.List<ItemPedido> itens;

    public java.util.List<ItemPedido> getItens() { return itens; }
    public void setItens(java.util.List<ItemPedido> itens) { this.itens = itens; }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_pedido")
    private LocalDateTime dataPedido;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    @Column(nullable = false)
    private String status;

    @PrePersist
    protected void onCreate() {
        this.dataPedido = LocalDateTime.now();
        this.status = "CRIADO";
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public Long getUsuarioId() {return usuarioId;}
    public void setUsuarioId(Long usuarioId) {this.usuarioId = usuarioId;}

    public LocalDateTime getDataPedido() {return dataPedido;}
    public void setDataPedido(LocalDateTime dataPedido) {this.dataPedido = dataPedido;}

    public BigDecimal getValorTotal() {return valorTotal;}
    public void setValorTotal(BigDecimal valorTotal) {this.valorTotal = valorTotal;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

}
