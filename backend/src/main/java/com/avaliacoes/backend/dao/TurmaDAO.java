package com.avaliacoes.backend.dao;

import com.avaliacoes.backend.model.Turma;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TurmaDAO{
    private final JdbcTemplate jdbcTemplate;

    public TurmaDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    private final RowMapper<Turma>rowMapper=(rs, rowNum)->{
        Turma turma=new Turma();
        turma.setCodigo(rs.getInt("codigo"));
        turma.setNome(rs.getString("nome"));
        turma.setAnoLetivo(rs.getInt("ano_letivo"));
        return turma;
    };

    public void inserir(Turma turma){
        if(turma.getCodigo()<=0){
            Integer proximo=jdbcTemplate.queryForObject("SELECT COALESCE(MAX(codigo), 0)+1 FROM turma", Integer.class);
            turma.setCodigo(proximo!=null?proximo:1);
        }
        String sql="INSERT INTO turma (codigo, nome, ano_letivo)VALUES(?, ?, ?)";
        jdbcTemplate.update(sql, turma.getCodigo(), turma.getNome(), turma.getAnoLetivo());
    }

    public List<Turma>listarTodas(){
        String sql="SELECT codigo, nome, ano_letivo FROM turma ORDER BY ano_letivo DESC, nome";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Turma>buscarPorCodigo(int codigo){
        String sql="SELECT codigo, nome, ano_letivo FROM turma WHERE codigo=?";
        List<Turma>resultados=jdbcTemplate.query(sql, rowMapper, codigo);
        return resultados.stream().findFirst();
    }

    public int atualizar(Turma turma){
        String sql="UPDATE turma SET nome=?, ano_letivo=? WHERE codigo=?";
        return jdbcTemplate.update(sql, turma.getNome(), turma.getAnoLetivo(), turma.getCodigo());
    }

    public int excluir(int codigo){
        String sql="DELETE FROM turma WHERE codigo=?";
        return jdbcTemplate.update(sql, codigo);
    }
}
