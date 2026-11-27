package com.avaliacoes.backend.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler{
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>>tratarArgumentoInvalido(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>>tratarAcessoNegado(SecurityException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>>tratarIntegridade(DataIntegrityViolationException ex){
        return ResponseEntity.badRequest().body(Map.of("erro", "Operacao violou uma restricao de integridade do banco de dados."));
    }
}
