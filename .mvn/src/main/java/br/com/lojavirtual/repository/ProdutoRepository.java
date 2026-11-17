package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // Trava a linha do produto durante a compra: dois clientes não conseguem comprar o mesmo último item
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarComTrava(@Param("id") Long id);
}
