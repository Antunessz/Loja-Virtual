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

    private static final int IMAGEM_MAX_CHARS = 600_000; // ~450 KB em base64
    private static final List<String> IMAGEM_PREFIXOS = List.of(
            "data:image/jpeg;base64,", "data:image/png;base64,", "data:image/webp;base64,");

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
        produto.setPrazoEntregaDias(dto.getPrazoEntregaDias() == null ? 0 : dto.getPrazoEntregaDias());
        if (dto.getImagem() != null && !dto.getImagem().isBlank()) {
            produto.setImagem(validarImagem(dto.getImagem()));
        }
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
        if (dto.getPrazoEntregaDias() != null) {
            produto.setPrazoEntregaDias(dto.getPrazoEntregaDias());
        }
        if (dto.getImagem() != null) { // ausente = mantém a foto; vazio = remove
            produto.setImagem(dto.getImagem().isBlank() ? null : validarImagem(dto.getImagem()));
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

    private String validarImagem(String imagem) {
        if (imagem.length() > IMAGEM_MAX_CHARS) {
            throw new RegraNegocioException("Imagem muito grande (máximo de aproximadamente 450 KB)");
        }
        if (IMAGEM_PREFIXOS.stream().noneMatch(imagem::startsWith)) {
            throw new RegraNegocioException("Formato de imagem inválido (use JPG, PNG ou WEBP)");
        }
        return imagem;
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Categoria não encontrada", HttpStatus.NOT_FOUND));
    }
}
