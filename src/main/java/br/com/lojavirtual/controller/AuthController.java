package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.UsuarioRegistroDTO;
import br.com.lojavirtual.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Mapeia a rota POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        try {
            authService.registrarUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso!");
        } catch (RuntimeException e) {
            // Captura o erro de e-mail duplicado (RN004) e retorna status 400 (Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}