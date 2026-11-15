package br.com.lojavirtual.repository;
import br.com.lojavirtual.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {}