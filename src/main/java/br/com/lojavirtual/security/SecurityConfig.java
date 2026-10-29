package br.com.lojavirtual.config;

import br.com.lojavirtual.security.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityFilter securityFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Desativa proteção contra formulários fantasma (usamos tokens)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Libera rotas públicas de login, cadastro e o Swagger
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // REGRAS DO CATÁLOGO
                        // [RF04] Qualquer um pode consultar produtos
                        .requestMatchers(HttpMethod.GET, "/api/produtos/**").permitAll()

                        // [RF12] Apenas administradores podem cadastrar produtos
                        .requestMatchers(HttpMethod.POST, "/api/produtos/**").hasRole("ADMIN")

                        // Qualquer outra rota exige que o usuário esteja logado
                        .anyRequest().authenticated()
                )
                // Pede para o Spring rodar o nosso filtro ANTES dos filtros padrão dele
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}