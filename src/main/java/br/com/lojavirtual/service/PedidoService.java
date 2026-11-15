package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.PedidoRequestDTO;
import br.com.lojavirtual.dto.PedidoRespostaDTO;
import br.com.lojavirtual.entity.ItemPedido;
import br.com.lojavirtual.entity.Pedido;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.repository.PedidoRepository;
import br.com.lojavirtual.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class PedidoService {

    private static final Set<String> STATUS_VALIDOS = Set.of("PENDENTE", "PAGO", "ENVIADO", "ENTREGUE", "CANCELADO");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    // RF10, RN006, RN007, RN010, RN011, RN012, RN013
    @Transactional
    public PedidoRespostaDTO criar(Usuario usuario, PedidoRequestDTO dto) {
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new RuntimeException("O carrinho está vazio");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setData(LocalDateTime.now());
        pedido.setStatus("PENDENTE");

        BigDecimal total = BigDecimal.ZERO;
        for (PedidoRequestDTO.Item i : dto.getItens()) {
            Produto produto = produtoRepository.findById(i.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            if (!"ATIVO".equals(produto.getStatus())) {
                throw new RuntimeException("Produto indisponível: " + produto.getNome());
            }
            if (i.getQuantidade() == null || i.getQuantidade() <= 0) {
                throw new RuntimeException("Quantidade inválida");
            }
            if (i.getQuantidade() > produto.getEstoque()) {
                throw new RuntimeException("Estoque insuficiente para " + produto.getNome());
            }

            produto.setEstoque(produto.getEstoque() - i.getQuantidade());

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(i.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            pedido.getItens().add(item);

            total = total.add(produto.getPreco().multiply(BigDecimal.valueOf(i.getQuantidade())));
        }
        pedido.setTotal(total);

        return paraDto(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoRespostaDTO> listarMeus(Usuario usuario) {
        return pedidoRepository.findByUsuarioIdOrderByDataDesc(usuario.getId()).stream().map(this::paraDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoRespostaDTO> listarTodos() {
        return pedidoRepository.findAllByOrderByDataDesc().stream().map(this::paraDto).toList();
    }

    @Transactional
    public PedidoRespostaDTO atualizarStatus(Long id, String status) {
        String novo = status == null ? "" : status.trim().toUpperCase();
        if (!STATUS_VALIDOS.contains(novo)) {
            throw new RuntimeException("Status inválido");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        pedido.setStatus(novo);
        return paraDto(pedidoRepository.save(pedido));
    }

    private PedidoRespostaDTO paraDto(Pedido p) {
        PedidoRespostaDTO d = new PedidoRespostaDTO();
        d.setId(p.getId());
        d.setData(p.getData());
        d.setStatus(p.getStatus());
        d.setTotal(p.getTotal());
        d.setCliente(p.getUsuario().getNome());
        for (ItemPedido it : p.getItens()) {
            PedidoRespostaDTO.Item x = new PedidoRespostaDTO.Item();
            x.setProdutoId(it.getProduto().getId());
            x.setNome(it.getProduto().getNome());
            x.setPreco(it.getPrecoUnitario());
            x.setQuantidade(it.getQuantidade());
            d.getItens().add(x);
        }
        return d;
    }
}
