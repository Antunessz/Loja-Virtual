package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.PedidoRequestDTO;
import br.com.lojavirtual.dto.PedidoRespostaDTO;
import br.com.lojavirtual.dto.StatusPedidoDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Todas as rotas exigem login (SecurityConfig). O usuário vem do token, nunca do corpo da requisição.
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // [RF10]
    @PostMapping
    public ResponseEntity<PedidoRespostaDTO> criar(@Valid @RequestBody PedidoRequestDTO dto,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(usuario, dto));
    }

    // [RF11] somente os pedidos do usuário logado
    @GetMapping("/meus")
    public List<PedidoRespostaDTO> meus(@AuthenticationPrincipal Usuario usuario) {
        return pedidoService.listarDoUsuario(usuario);
    }

    // [RF15] somente admin (regra no SecurityConfig)
    @GetMapping
    public List<PedidoRespostaDTO> todos() {
        return pedidoService.listarTodos();
    }

    // [RF15] [RN013] somente admin (regra no SecurityConfig)
    @PutMapping("/{id}/status")
    public PedidoRespostaDTO status(@PathVariable Long id, @Valid @RequestBody StatusPedidoDTO dto) {
        return pedidoService.atualizarStatus(id, dto.getStatus());
    }
}
