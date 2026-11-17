package br.com.lojavirtual.service;

import br.com.lojavirtual.dto.PerfilAtualizacaoDTO;
import br.com.lojavirtual.dto.PerfilRespostaDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.exception.RegraNegocioException;
import br.com.lojavirtual.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // Atualiza os dados cadastrais do PRÓPRIO usuário logado (nome e, opcionalmente, senha)
    @Transactional
    public PerfilRespostaDTO atualizarPerfil(Usuario logado, PerfilAtualizacaoDTO dto) {
        Usuario usuario = repository.findById(logado.getId())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        usuario.setNome(dto.getNome().trim());

        if (dto.getNovaSenha() != null && !dto.getNovaSenha().isBlank()) {
            if (dto.getSenhaAtual() == null || !passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenha())) {
                throw new RegraNegocioException("Senha atual incorreta");
            }
            usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
        }

        repository.save(usuario);
        return new PerfilRespostaDTO(usuario.getNome(), usuario.getEmail(), tokenService.gerarToken(usuario));
    }
}
