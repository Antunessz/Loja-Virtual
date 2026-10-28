package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.UsuarioLoginDTO;
import br.com.lojavirtual.dto.UsuarioRegistroDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    // Correção: TokenService adicionado aos parâmetros do construtor
    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public UsuarioRepository getRepository() {
        return repository;
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
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

    // NOVO MÉTODO PARA LOGIN [RN002]
    public String autenticarUsuario(UsuarioLoginDTO dto) {
        // 1. Busca o usuário no banco
        Usuario usuario = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));

        // 2. Compara a senha digitada com a criptografada no banco
        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta!");
        }

        // 3. Se tudo estiver certo, fabrica e entrega o crachá (Token)
        return tokenService.gerarToken(usuario);
    }
}