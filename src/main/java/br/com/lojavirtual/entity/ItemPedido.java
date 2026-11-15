package br.com.lojavirtual.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "ITEM_PEDIDO")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private BigDecimal subtotal;

    public long getId(){return id;}
    public void setId(long id){this.id=id;}

    public Long getPedidoId(){return pedidoId;}
    public void setPedidoId(Long pedidoId){this.pedidoId=pedidoId;}

    public Long getProdutoId(){return produtoId;}
    public void setProdutoId(Long produtoId){this.produtoId=produtoId;}

    public Integer getQuantidade(){return quantidade;}
    public void setQuantidade(Integer quantidade){this.quantidade=quantidade;}

    public BigDecimal getSubtotal() {return subtotal;}
    public void setSubtotal(BigDecimal subtotal) {this.subtotal = subtotal;}

    public BigDecimal getPrecoUnitario() {return precoUnitario;}


}
