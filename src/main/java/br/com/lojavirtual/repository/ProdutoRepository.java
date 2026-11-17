package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Busca por nome OU descrição (ignora maiúsculas/minúsculas)
    List<Produto> findByNomeContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String nome, String descricao);

    // Produtos com estoque maior que zero
    List<Produto> findByEstoqueGreaterThan(Integer quantidade);

    @Override
    @EntityGraph(attributePaths = "categoria")
    List<Produto> findAll();

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByStatus(String status);

    // Trava a linha do produto durante a compra: dois clientes não conseguem comprar o mesmo último item.
    // SQL nativo "FOR UPDATE" funciona igual no MySQL e no MariaDB (o lock do Hibernate gera "FOR UPDATE OF",
    // que o MariaDB não aceita). Precisa rodar dentro de uma @Transactional (o PedidoService já roda).
    @Query(value = "SELECT * FROM produto WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Produto> buscarComTrava(@Param("id") Long id);
}
