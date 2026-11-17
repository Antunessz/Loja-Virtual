package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.PerfilAtualizacaoDTO;
import br.com.lojavirtual.dto.PerfilRespostaDTO;
import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Exige login (SecurityConfig): cada usuário só enxerga e altera o próprio cadastro
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public PerfilRespostaDTO me(@AuthenticationPrincipal Usuario usuario) {
        return new PerfilRespostaDTO(usuario.getNome(), usuario.getEmail(), null);
    }

    @PutMapping("/me")
    public PerfilRespostaDTO atualizar(@Valid @RequestBody PerfilAtualizacaoDTO dto,
                                       @AuthenticationPrincipal Usuario usuario) {
        return usuarioService.atualizarPerfil(usuario, dto);
    }
}
