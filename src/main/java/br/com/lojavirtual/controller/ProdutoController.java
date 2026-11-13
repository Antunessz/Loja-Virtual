package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.ProdutoRegistroDTO;
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
}