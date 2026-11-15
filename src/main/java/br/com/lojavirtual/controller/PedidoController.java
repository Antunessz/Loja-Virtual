package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.PedidoRequestDTO;
import br.com.lojavirtual.dto.StatusPedidoDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    private ResponseEntity<String> naoLogado() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Faça login para continuar.");
    }

    private ResponseEntity<String> negado() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: apenas administradores.");
    }

    // RF10: finalizar pedido (cliente logado)
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody PedidoRequestDTO dto, @AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) return naoLogado();
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(usuario, dto));
    }

    // RF11: pedidos do cliente logado
    @GetMapping("/meus")
    public ResponseEntity<?> meus(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) return naoLogado();
        return ResponseEntity.ok(pedidoService.listarMeus(usuario));
    }

    // RF15: todos os pedidos (admin)
    @GetMapping
    public ResponseEntity<?> todos(@AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) return naoLogado();
        if (!usuario.isAdmin()) return negado();
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // RF15 / RN013: atualizar status (admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> status(@PathVariable Long id, @Valid @RequestBody StatusPedidoDTO dto,
                                    @AuthenticationPrincipal Usuario usuario) {
        if (usuario == null) return naoLogado();
        if (!usuario.isAdmin()) return negado();
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, dto.getStatus()));
    }
}
