package br.com.lojavirtual.service;

import br.com.lojavirtual.entity.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    private static final String ISSUER = "loja-virtual";

    private final Algorithm algorithm;

    // A chave vem do application.properties / variável de ambiente JWT_SECRET (nunca fixa no código)
    public TokenService(@Value("${app.jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret precisa ter pelo menos 32 caracteres");
        }
        this.algorithm = Algorithm.HMAC256(secret);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(usuario.getEmail())
                .withClaim("nome", usuario.getNome())
                .withClaim("perfil", usuario.getPerfil())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(2, ChronoUnit.HOURS)) // independe do fuso do servidor
                .sign(algorithm);
    }

    // Retorna o e-mail do token válido, ou "" se for inválido, expirado ou adulterado
    public String validarToken(String token) {
        try {
            String subject = JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject();
            return subject == null ? "" : subject;
        } catch (JWTVerificationException e) {
            return "";
        }
    }
}
