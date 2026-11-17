package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.UsuarioLoginDTO;
import br.com.lojavirtual.dto.UsuarioRegistroDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.exception.RegraNegocioException;
import br.com.lojavirtual.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // Mensagem única: não revela se o e-mail existe (evita enumeração de usuários)
    private static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha inválidos";

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public Usuario registrarUsuario(UsuarioRegistroDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        // [RN004] e-mail único
        if (repository.findByEmail(email).isPresent()) {
            throw new RegraNegocioException("E-mail já está em uso!");
        }

        Usuario novo = new Usuario();
        novo.setNome(dto.getNome().trim());
        novo.setEmail(email);
        novo.setSenha(passwordEncoder.encode(dto.getSenha())); // [RN005]
        novo.setPerfil("ROLE_CLIENTE");

        try {
            return repository.save(novo);
        } catch (DataIntegrityViolationException e) { // dois cadastros simultâneos com o mesmo e-mail
            throw new RegraNegocioException("E-mail já está em uso!");
        }
    }

    // [RN002] autenticação
    public String autenticarUsuario(UsuarioLoginDTO dto) {
        Usuario usuario = repository.findByEmail(dto.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RegraNegocioException(CREDENCIAIS_INVALIDAS, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new RegraNegocioException(CREDENCIAIS_INVALIDAS, HttpStatus.UNAUTHORIZED);
        }
        return tokenService.gerarToken(usuario);
    }
}
