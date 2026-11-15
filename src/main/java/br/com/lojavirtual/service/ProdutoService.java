package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.ProdutoAtualizacaoDTO;
import br.com.lojavirtual.dto.ProdutoRegistroDTO;
import br.com.lojavirtual.entity.Categoria;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.repository.CategoriaRepository;
import br.com.lojavirtual.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Produto cadastrarProduto(ProdutoRegistroDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        produto.setStatus("ATIVO");
        produto.setCategoria(categoria);

        return produtoRepository.save(produto);
    }
    public List<Produto> listar() {
        return produtoRepository.findAll();
    }

    public Produto atualizarProduto(Long id, ProdutoAtualizacaoDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        if (dto.getAtivo() != null) {
            produto.setStatus(dto.getAtivo() ? "ATIVO" : "INATIVO");
        }
        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            produto.setCategoria(categoria);
        }
        return produtoRepository.save(produto);
    }
}
