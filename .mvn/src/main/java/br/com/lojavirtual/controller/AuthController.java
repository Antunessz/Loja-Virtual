package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.UsuarioLoginDTO;
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

    // [RF02] devolve o token JWT somente se e-mail e senha conferirem
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UsuarioLoginDTO dto) {
        return ResponseEntity.ok(authService.autenticarUsuario(dto));
    }

    // [RF01]
    @PostMapping("/register")
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        authService.registrarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso!");
    }
}
