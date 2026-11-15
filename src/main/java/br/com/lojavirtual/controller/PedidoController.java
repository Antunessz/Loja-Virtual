package br.com.lojavirtual.controller;

import br.com.lojavirtual.entity.ItemPedido;
import br.com.lojavirtual.entity.Pedido;
import br.com.lojavirtual.repository.ItemPedidoRepository;
import br.com.lojavirtual.repository.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemRepository;

    public PedidoController(PedidoRepository pedidoRepository, ItemPedidoRepository itemRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemRepository = itemRepository;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        if (pedido.getUsuarioId() == null) pedido.setUsuarioId(1L); // Trava de segurança para a apresentação

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        if (pedido.getItens() != null) {
            for (ItemPedido item : pedido.getItens()) {
                item.setPedidoId(pedidoSalvo.getId());
                itemRepository.save(item);
            }
        }
        return ResponseEntity.ok(pedidoSalvo);
    }

    // Resolve o Erro 5 (Falta o GET /api/pedidos/meus)
    @GetMapping("/meus")
    public ResponseEntity<List<Pedido>> meusPedidos() {
        return ResponseEntity.ok(pedidoRepository.findAll());
    }
}