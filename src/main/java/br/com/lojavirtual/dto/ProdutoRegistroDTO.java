package br.com.lojavirtual.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProdutoRegistroDTO {

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @NotNull(message = "O estoque é obrigatório")
    @Min(value = 0, message = "O estoque não pode ser negativo")
    private Integer estoque;

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoriaId;

    // Foto (Data URL). No cadastro: opcional. Na edição: ausente = manter, vazio = remover.
    private String imagem;

    @Min(value = 0, message = "O prazo de entrega não pode ser negativo")
    @Max(value = 365, message = "O prazo de entrega deve ser de até 365 dias")
    private Integer prazoEntregaDias;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getImagem() { return imagem; }
    public void setImagem(String imagem) { this.imagem = imagem; }

    public Integer getPrazoEntregaDias() { return prazoEntregaDias; }
    public void setPrazoEntregaDias(Integer prazoEntregaDias) { this.prazoEntregaDias = prazoEntregaDias; }
}
