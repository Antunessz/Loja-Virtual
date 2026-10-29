package br.com.lojavirtual.security;

import br.com.lojavirtual.entity.Usuario;
import br.com.lojavirtual.repository.UsuarioRepository;
import br.com.lojavirtual.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recuperarToken(request);

        if (token != null) {
            String email = tokenService.validarToken(token);

            if (!email.isEmpty()) {
                Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

                if (usuario != null) {
                    // Transforma o perfil do banco (ex: "ROLE_CLIENTE" ou "ROLE_ADMIN") em uma autoridade do Spring
                    var authorities = Collections.singletonList(new SimpleGrantedAuthority(usuario.getPerfil()));
                    var authentication = new UsernamePasswordAuthenticationToken(usuario, null, authorities);

                    // Salva o usuário logado no contexto da requisição atual
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        // Continua o fluxo da requisição
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}