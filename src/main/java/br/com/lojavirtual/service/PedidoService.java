package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.PedidoRequestDTO;
import br.com.lojavirtual.dto.PedidoRespostaDTO;
import br.com.lojavirtual.entity.ItemPedido;
import br.com.lojavirtual.entity.Pedido;
import br.com.lojavirtual.entity.Produto;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.exception.RegraNegocioException;
import br.com.lojavirtual.repository.PedidoRepository;
import br.com.lojavirtual.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class PedidoService {

    private static final Set<String> STATUS_VALIDOS = Set.of("PENDENTE", "PAGO", "ENVIADO", "ENTREGUE", "CANCELADO");

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    // [RF10] [RN006] [RN007] [RN010] [RN011] [RN012] [RN013]
    // Preço, subtotal, total, dono e status são calculados AQUI; nada disso é aceito do cliente.
    @Transactional
    public PedidoRespostaDTO criar(Usuario usuario, PedidoRequestDTO dto) {
        // Junta linhas repetidas do mesmo produto e ordena por id (ordem fixa evita deadlock entre compras)
        Map<Long, Integer> quantidades = new TreeMap<>();
        for (PedidoRequestDTO.Item i : dto.getItens()) {
            quantidades.merge(i.getProdutoId(), i.getQuantidade(), Integer::sum);
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus("PENDENTE");

        BigDecimal total = BigDecimal.ZERO;
        int maiorPrazo = 0;
        for (Map.Entry<Long, Integer> entrada : quantidades.entrySet()) {
            Produto produto = produtoRepository.buscarComTrava(entrada.getKey())
                    .orElseThrow(() -> new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND));
            int quantidade = entrada.getValue();

            if (!"ATIVO".equals(produto.getStatus())) {
                throw new RegraNegocioException("Produto indisponível: " + produto.getNome());
            }
            if (quantidade > produto.getEstoque()) {
                throw new RegraNegocioException("Estoque insuficiente para " + produto.getNome());
            }

            produto.setEstoque(produto.getEstoque() - quantidade);
            maiorPrazo = Math.max(maiorPrazo, produto.getPrazoEntregaDias() == null ? 0 : produto.getPrazoEntregaDias());

            BigDecimal subtotal = produto.getPreco()
                    .multiply(BigDecimal.valueOf(quantidade))
                    .setScale(2, RoundingMode.HALF_UP);

            ItemPedido item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(subtotal);
            pedido.getItens().add(item);

            total = total.add(subtotal);
        }
        pedido.setValorTotal(total);
        pedido.setPrevisaoEntrega(maiorPrazo > 0 ? somarDiasUteis(LocalDate.now(), maiorPrazo) : null);

        return paraDto(pedidoRepository.save(pedido));
    }

    // Cada cliente enxerga SOMENTE os próprios pedidos
    @Transactional(readOnly = true)
    public List<PedidoRespostaDTO> listarDoUsuario(Usuario usuario) {
        return pedidoRepository.findByUsuarioIdOrderByDataPedidoDesc(usuario.getId())
                .stream().map(this::paraDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PedidoRespostaDTO> listarTodos() {
        return pedidoRepository.findAllByOrderByDataPedidoDesc()
                .stream().map(this::paraDto).toList();
    }

    // [RF15] [RN013]
    @Transactional
    public PedidoRespostaDTO atualizarStatus(Long id, String status) {
        String novo = status == null ? "" : status.trim().toUpperCase();
        if (!STATUS_VALIDOS.contains(novo)) {
            throw new RegraNegocioException("Status inválido");
        }
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Pedido não encontrado", HttpStatus.NOT_FOUND));

        if ("CANCELADO".equals(pedido.getStatus())) {
            throw new RegraNegocioException("Pedido cancelado não pode ser alterado");
        }
        if ("CANCELADO".equals(novo)) { // cancelar devolve os itens ao estoque
            for (ItemPedido item : pedido.getItens()) {
                Produto produto = produtoRepository.buscarComTrava(item.getProduto().getId())
                        .orElseThrow(() -> new RegraNegocioException("Produto não encontrado", HttpStatus.NOT_FOUND));
                produto.setEstoque(produto.getEstoque() + item.getQuantidade());
            }
        }
        pedido.setStatus(novo);
        return paraDto(pedidoRepository.save(pedido));
    }

    // Soma dias úteis (pula sábado e domingo; feriados não são considerados)
    private LocalDate somarDiasUteis(LocalDate inicio, int dias) {
        LocalDate data = inicio;
        int somados = 0;
        while (somados < dias) {
            data = data.plusDays(1);
            DayOfWeek dia = data.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) somados++;
        }
        return data;
    }

    private PedidoRespostaDTO paraDto(Pedido p) {
        PedidoRespostaDTO d = new PedidoRespostaDTO();
        d.setId(p.getId());
        d.setData(p.getDataPedido());
        d.setStatus(p.getStatus());
        d.setTotal(p.getValorTotal());
        d.setCliente(p.getUsuario().getNome());
        d.setPrevisaoEntrega(p.getPrevisaoEntrega());
        for (ItemPedido it : p.getItens()) {
            PedidoRespostaDTO.Item x = new PedidoRespostaDTO.Item();
            x.setProdutoId(it.getProduto().getId());
            x.setNome(it.getProduto().getNome());
            x.setPreco(it.getPrecoUnitario());
            x.setQuantidade(it.getQuantidade());
            x.setSubtotal(it.getSubtotal());
            d.getItens().add(x);
        }
        return d;
    }
}
