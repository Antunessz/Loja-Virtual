package br.com.lojavirtual.controller;

import br.com.lojavirtual.entity.Pedido;
import br.com.lojavirtual.repository.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*") // Essencial para evitar bloqueios do frontend
public class PedidoController {

    private final PedidoRepository repository;

    public PedidoController(PedidoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Pedido> criarPedido(@RequestBody Pedido pedido) {
        // Salva a venda diretamente no banco para a apresentação
        Pedido pedidoSalvo = repository.save(pedido);
        return ResponseEntity.ok(pedidoSalvo);
    }
}
