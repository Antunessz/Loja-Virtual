package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.ProdutoAtualizacaoDTO;
import br.com.lojavirtual.dto.ProdutoRegistroDTO;
import br.com.lojavirtual.entity.Usuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody ProdutoRegistroDTO dto) {
        Produto produtoCadastrado = produtoService.cadastrarProduto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoCadastrado);
    }
    @GetMapping
    public List<Produto> listar() {
        return produtoService.listar();
    }

    // RF13 / RF14 / RN014: somente administrador altera produto, preço e estoque
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @Valid @RequestBody ProdutoAtualizacaoDTO dto,
                                       @AuthenticationPrincipal Usuario usuario) {
        if (usuario == null || !usuario.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: apenas administradores.");
        }
        return ResponseEntity.ok(produtoService.atualizarProduto(id, dto));
    }
}
