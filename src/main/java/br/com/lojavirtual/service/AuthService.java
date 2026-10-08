package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.UsuarioRegistroDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(UsuarioRegistroDTO dto) {
        // Regra [RN004]: Verifica e-mail único
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já está em uso!");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(dto.getNome());
        novoUsuario.setEmail(dto.getEmail());

        // Regra [RN005]: Protege a senha
        novoUsuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        novoUsuario.setPerfil("ROLE_CLIENTE");

        return repository.save(novoUsuario);
    }
}