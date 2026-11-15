package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByDataDesc(Long usuarioId);

    List<Pedido> findAllByOrderByDataDesc();
}
