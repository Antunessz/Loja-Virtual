package br.com.lojavirtual.controller;

import br.com.lojavirtual.dto.UsuarioRegistroDTO;
import br.com.lojavirtual.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.lojavirtual.dto.UsuarioLoginDTO;

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
        return ResponseEntity.ok("token_falso_para_apresentacao");
    }

}