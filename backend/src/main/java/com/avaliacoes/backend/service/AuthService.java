package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.AuthDAO;
import com.avaliacoes.backend.model.LoginDTO;
import com.avaliacoes.backend.model.UsuarioSessaoDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService{
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO){
        this.authDAO=authDAO;
    }

    public Optional<UsuarioSessaoDTO>login(LoginDTO dto){
        if(dto==null||dto.getEmail()==null||dto.getSenha()==null){
            return Optional.empty();
        }
        return authDAO.autenticar(dto.getEmail().trim(), dto.getSenha());
    }
}
