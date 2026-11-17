package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.ProdutoAtualizacaoDTO;
import br.com.lojavirtual.dto.ProdutoRegistroDTO;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // [RF04] público: só produtos ativos. Administrador autenticado vê todos.
    @GetMapping
    public List<Produto> listar(@AuthenticationPrincipal Usuario usuario) {
        return produtoService.listar(usuario != null && usuario.isAdmin());
    }

    // [RF12] somente admin (regra no SecurityConfig)
    @PostMapping
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody ProdutoRegistroDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.cadastrarProduto(dto));
    }

    // [RF13] [RF14] somente admin (regra no SecurityConfig)
    @PutMapping("/{id}")
    public Produto atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoAtualizacaoDTO dto) {
        return produtoService.atualizarProduto(id, dto);
    }
}
