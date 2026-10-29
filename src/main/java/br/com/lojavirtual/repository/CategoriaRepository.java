package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Como a Categoria só tem operações básicas no início, o JpaRepository já nos dá os métodos de salvar, buscar por ID, listar todos e deletar.
}