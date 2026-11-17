package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // EntityGraph carrega usuário, itens e produtos numa consulta só (evita o problema N+1)
    @EntityGraph(attributePaths = {"usuario", "itens", "itens.produto"})
    List<Pedido> findByUsuarioIdOrderByDataPedidoDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "itens", "itens.produto"})
    List<Pedido> findAllByOrderByDataPedidoDesc();
}
