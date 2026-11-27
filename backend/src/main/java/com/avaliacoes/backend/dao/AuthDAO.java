package com.avaliacoes.backend.dao;

import com.avaliacoes.backend.model.UsuarioSessaoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AuthDAO{
    private final JdbcTemplate jdbcTemplate;

    public AuthDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public Optional<UsuarioSessaoDTO>autenticar(String email, String senha){
        String sqlSenha="SELECT email FROM Senha WHERE email=? AND senha=?";
        List<String>emails=jdbcTemplate.query(sqlSenha, (rs, rowNum)->rs.getString("email"), email, senha);
        if(emails.isEmpty()){
            return Optional.empty();
        }

        String sqlProf="SELECT ID, nome, email FROM Professor WHERE email=?";
        List<UsuarioSessaoDTO>profs=jdbcTemplate.query(sqlProf, (rs, rowNum)->{
            UsuarioSessaoDTO u=new UsuarioSessaoDTO();
            u.setId(rs.getInt("ID"));
            u.setNome(rs.getString("nome"));
            u.setEmail(rs.getString("email"));
            u.setTipo("PROFESSOR");
            return u;
        }, email);
        if(!profs.isEmpty()){
            return Optional.of(profs.get(0));
        }

        String sqlAluno="SELECT a.ID, a.nome, a.email, mt.codigo_turma FROM Aluno a JOIN Matricula_turma mt ON mt.matricula=a.matricula WHERE a.email=?";
        List<UsuarioSessaoDTO>alunos=jdbcTemplate.query(sqlAluno, (rs, rowNum)->{
            UsuarioSessaoDTO u=new UsuarioSessaoDTO();
            u.setId(rs.getInt("ID"));
            u.setNome(rs.getString("nome"));
            u.setEmail(rs.getString("email"));
            u.setTipo("ALUNO");
            u.setTurmaCodigo(rs.getInt("codigo_turma"));
            return u;
        }, email);
        return alunos.stream().findFirst();
    }
}
