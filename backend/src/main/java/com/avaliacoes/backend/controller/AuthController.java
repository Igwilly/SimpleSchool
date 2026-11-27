package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.model.LoginDTO;
import com.avaliacoes.backend.model.UsuarioSessaoDTO;
import com.avaliacoes.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService=authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody LoginDTO dto){
        return authService.login(dto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Email ou senha invalidos.")));
    }
}
