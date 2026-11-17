package br.com.lojavirtual.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PRODUTO")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    // BigDecimal é o tipo ideal em Java para trabalhar com dinheiro/preços
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoque;

    @Column(length = 50)
    private String status;

    // Foto em Data URL (data:image/jpeg;base64,...). O frontend reduz para no máximo 600 px antes de enviar.
    @Column(columnDefinition = "MEDIUMTEXT")
    private String imagem;

    // Prazo médio de entrega, em dias úteis
    @Column(name = "prazo_entrega_dias")
    private Integer prazoEntregaDias;

    // Relacionamento: Muitos Produtos pertencem a Uma Categoria (N:1)
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }

    public Integer getPrazoEntregaDias() { return prazoEntregaDias; }
    public void setPrazoEntregaDias(Integer prazoEntregaDias) { this.prazoEntregaDias = prazoEntregaDias; }

    // Campo "ativo" no JSON (usado pelo frontend); no banco continua sendo a coluna status
    public boolean getAtivo() { return "ATIVO".equals(status); }
}
