package br.com.lojavirtual.service;

import br.com.lojavirtual.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // Assinatura secreta da sua aplicação (em um projeto real, fica escondida em variáveis de ambiente)
    private String secret = "minha-chave-secreta-loja-virtual";

    public String gerarToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withIssuer("loja-virtual") // Quem emitiu
                .withSubject(usuario.getEmail()) // A quem pertence (e-mail)
                .withClaim("perfil", usuario.getPerfil()) // Nível de acesso
                .withExpiresAt(gerarDataExpiracao()) // Validade
                .sign(algorithm);
    }

    private Instant gerarDataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
