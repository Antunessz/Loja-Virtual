package br.com.lojavirtual.repository;

import br.com.lojavirtual.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Método para validar a regra [RN004] E-mail único
    Optional<Usuario> findByEmail(String email);
}