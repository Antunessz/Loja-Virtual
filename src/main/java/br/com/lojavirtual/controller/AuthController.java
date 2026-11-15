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
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UsuarioLoginDTO dto) {
        String mockJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwibm9tZSI6IlRlc3RlIDMiLCJwZXJmaWwiOiJST0xFX0FETUlOIn0.signature";
        return ResponseEntity.ok(mockJwt);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registrar(@Valid @RequestBody UsuarioRegistroDTO dto) {
        try {
            authService.registrarUsuario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuário cadastrado com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}