package br.com.lojavirtual.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

// O cliente envia SÓ produto e quantidade. Preço, total, dono e status são decididos pelo servidor.
public class PedidoRequestDTO {

    @NotEmpty(message = "O carrinho está vazio")
    @Size(max = 100, message = "Pedido com itens demais")
    @Valid
    private List<Item> itens;

    public List<Item> getItens() { return itens; }
    public void setItens(List<Item> itens) { this.itens = itens; }

    public static class Item {
        @NotNull(message = "O produto é obrigatório")
        private Long produtoId;

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser maior que zero")
        @Max(value = 1000, message = "Quantidade máxima por item: 1000")
        private Integer quantidade;

        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }
}
