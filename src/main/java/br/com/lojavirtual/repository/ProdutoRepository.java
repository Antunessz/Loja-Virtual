package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Método customizado para buscar produtos onde o nome OU a descrição contenham uma determinada palavra (ignora maiúsculas/minúsculas)
    List<Produto> findByNomeContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String nome, String descricao);

    // Método para listar apenas produtos com estoque maior que zero (útil para o Catálogo do Cliente)
    List<Produto> findByEstoqueGreaterThan(Integer quantidade);
}