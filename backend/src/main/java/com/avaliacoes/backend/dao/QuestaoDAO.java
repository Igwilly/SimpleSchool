package com.avaliacoes.backend.dao;

import com.avaliacoes.backend.model.Alternativa;
import com.avaliacoes.backend.model.QuestaoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class QuestaoDAO{
    private final JdbcTemplate jdbcTemplate;

    public QuestaoDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    @Transactional
    public QuestaoDTO inserir(QuestaoDTO dto){
        if(dto.getCodigo()<=0){
            Integer proximo=jdbcTemplate.queryForObject("SELECT COALESCE(MAX(codigo), 0)+1 FROM Questao", Integer.class);
            dto.setCodigo(proximo!=null?proximo:1);
        }

        jdbcTemplate.update("INSERT INTO Questao (codigo, enunciado) VALUES (?, ?)", dto.getCodigo(), dto.getEnunciado());

        if("OBJETIVA".equalsIgnoreCase(dto.getTipo())){
            dto.setTipo("OBJETIVA");
            jdbcTemplate.update("INSERT INTO Questao_objetiva (codigo) VALUES (?)", dto.getCodigo());
            for(Alternativa alt:dto.getAlternativas()){
                String letra=alt.getLetra().trim().toUpperCase();
                alt.setCodigoQuestao(dto.getCodigo());
                alt.setLetra(letra);
                jdbcTemplate.update(
                        "INSERT INTO Alternativa (codigo_questao, letra, texto, correta) VALUES (?, ?, ?, ?)",
                        dto.getCodigo(), letra, alt.getTexto(), alt.isCorreta()
                );
            }
        }else{
            dto.setTipo("DISSERTATIVA");
            jdbcTemplate.update(
                    "INSERT INTO Questao_dissertativa (codigo, resposta_esperada) VALUES (?, ?)",
                    dto.getCodigo(), dto.getRespostaEsperada()
            );
        }
        return dto;
    }

    public List<QuestaoDTO>listarTodas(){
        String sql="""
                SELECT q.codigo, q.enunciado,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo,
                       qd.resposta_esperada
                FROM Questao q
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                LEFT JOIN Questao_dissertativa qd ON qd.codigo=q.codigo
                ORDER BY q.codigo DESC
                """;

        Map<Integer, QuestaoDTO>mapa=new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs->{
            QuestaoDTO q=new QuestaoDTO();
            q.setCodigo(rs.getInt("codigo"));
            q.setEnunciado(rs.getString("enunciado"));
            q.setTipo(rs.getString("tipo"));
            q.setRespostaEsperada(rs.getString("resposta_esperada"));
            mapa.put(q.getCodigo(), q);
        });

        if(!mapa.isEmpty()){
            String sqlAlt="SELECT codigo_questao, letra, texto, correta FROM Alternativa ORDER BY codigo_questao, letra";
            jdbcTemplate.query(sqlAlt, rs->{
                QuestaoDTO pai=mapa.get(rs.getInt("codigo_questao"));
                if(pai!=null){
                    Alternativa alt=new Alternativa();
                    alt.setCodigoQuestao(pai.getCodigo());
                    alt.setLetra(rs.getString("letra").trim());
                    alt.setTexto(rs.getString("texto"));
                    alt.setCorreta(rs.getBoolean("correta"));
                    pai.getAlternativas().add(alt);
                }
            });
        }

        return List.copyOf(mapa.values());
    }

    public Optional<QuestaoDTO>buscarPorCodigo(int codigo){
        String sql="""
                SELECT q.codigo, q.enunciado,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo,
                       qd.resposta_esperada
                FROM Questao q
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                LEFT JOIN Questao_dissertativa qd ON qd.codigo=q.codigo
                WHERE q.codigo=?
                """;
        List<QuestaoDTO>lista=jdbcTemplate.query(sql, (rs, rowNum)->{
            QuestaoDTO q=new QuestaoDTO();
            q.setCodigo(rs.getInt("codigo"));
            q.setEnunciado(rs.getString("enunciado"));
            q.setTipo(rs.getString("tipo"));
            q.setRespostaEsperada(rs.getString("resposta_esperada"));
            return q;
        }, codigo);

        if(lista.isEmpty()){
            return Optional.empty();
        }

        QuestaoDTO q=lista.get(0);
        if("OBJETIVA".equals(q.getTipo())){
            List<Alternativa>alts=jdbcTemplate.query(
                    "SELECT codigo_questao, letra, texto, correta FROM Alternativa WHERE codigo_questao=? ORDER BY letra",
                    (rs, rowNum)->{
                        Alternativa alt=new Alternativa();
                        alt.setCodigoQuestao(rs.getInt("codigo_questao"));
                        alt.setLetra(rs.getString("letra").trim());
                        alt.setTexto(rs.getString("texto"));
                        alt.setCorreta(rs.getBoolean("correta"));
                        return alt;
                    },
                    codigo
            );
            q.setAlternativas(alts);
        }
        return Optional.of(q);
    }

    @Transactional
    public int excluir(int codigo){
        jdbcTemplate.update("DELETE FROM Alternativa WHERE codigo_questao=?", codigo);
        jdbcTemplate.update("DELETE FROM Questao_objetiva WHERE codigo=?", codigo);
        jdbcTemplate.update("DELETE FROM Questao_dissertativa WHERE codigo=?", codigo);
        return jdbcTemplate.update("DELETE FROM Questao WHERE codigo=?", codigo);
    }
}
