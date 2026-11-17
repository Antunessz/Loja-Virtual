package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.ProdutoAtualizacaoDTO;
import br.com.lojavirtual.dto.ProdutoRegistroDTO;
import br.com.lojavirtual.entity.Categoria;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.exception.RegraNegocioException;
import br.com.lojavirtual.repository.CategoriaRepository;
import br.com.lojavirtual.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
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
        Produto produto = new Produto();
        produto.setNome(dto.getNome().trim());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        produto.setStatus("ATIVO");
        produto.setCategoria(buscarCategoria(dto.getCategoriaId()));
        return produtoRepository.save(produto);
    }

    // [RF13] [RF14]
    public Produto atualizarProduto(Long id, ProdutoAtualizacaoDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND));
        produto.setNome(dto.getNome().trim());
        produto.setDescricao(dto.getDescricao());
        produto.setPreco(dto.getPreco());
        produto.setEstoque(dto.getEstoque());
        if (dto.getAtivo() != null) {
            produto.setStatus(dto.getAtivo() ? "ATIVO" : "INATIVO");
        }
        if (dto.getCategoriaId() != null) {
            produto.setCategoria(buscarCategoria(dto.getCategoriaId()));
        }
        return produtoRepository.save(produto);
    }

    // Cliente e visitante só veem produtos ativos; administrador vê todos
    public List<Produto> listar(boolean incluirInativos) {
        return incluirInativos ? produtoRepository.findAll() : produtoRepository.findByStatus("ATIVO");
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada", HttpStatus.NOT_FOUND));
    }
}
