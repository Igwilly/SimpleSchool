package com.avaliacoes.backend.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RelatorioDAO{
    private final JdbcTemplate jdbcTemplate;

    public RelatorioDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<Map<String, Object>>listarAlunos(){
        String sql="""
                SELECT al.ID AS id, al.nome, al.email, t.codigo AS codigo_turma, t.nome AS turma
                FROM Aluno al
                INNER JOIN Matricula_turma mt ON mt.matricula=al.matricula
                INNER JOIN Turma t ON t.codigo=mt.codigo_turma
                ORDER BY al.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("nome", rs.getString("nome"));
            m.put("email", rs.getString("email"));
            m.put("codigoTurma", rs.getInt("codigo_turma"));
            m.put("turma", rs.getString("turma"));
            return m;
        });
    }

    public List<Map<String, Object>>relatorioDesempenhoAvaliacoes(LocalDate dataInicio, LocalDate dataFim, Integer codigoAvaliacao, Integer codigoTurma, int minRespondentes){
        List<Object>params=new ArrayList<>();
        params.add(Date.valueOf(dataInicio));
        params.add(Date.valueOf(dataFim));

        String filtroTurma="";
        if(codigoTurma!=null&&codigoTurma>0){
            filtroTurma=" AND mt.codigo_turma=? ";
            params.add(codigoTurma);
        }

        String filtroAvaliacao="";
        if(codigoAvaliacao!=null&&codigoAvaliacao>0){
            filtroAvaliacao=" WHERE av.codigo=? ";
            params.add(codigoAvaliacao);
        }

        params.add(minRespondentes);

        String sql="""
                SELECT
                    av.codigo,
                    av.titulo,
                    COUNT(notas_aluno.id_aluno) AS qtd_respondentes,
                    ROUND(AVG(notas_aluno.nota_total), 2) AS media_avaliacao,
                    ROUND(MIN(notas_aluno.nota_total), 2) AS menor_nota,
                    ROUND(MAX(notas_aluno.nota_total), 2) AS maior_nota,
                    ROUND((
                        SELECT AVG(sub.total)
                        FROM (
                            SELECT SUM(r2.nota) AS total
                            FROM Resposta r2
                            GROUP BY r2.ID_aluno, r2.codigo_avaliacao
                        ) sub
                    ), 2) AS media_geral_sistema
                FROM Avaliacao av
                INNER JOIN (
                    SELECT
                        s.ID_aluno AS id_aluno,
                        s.codigo_avaliacao,
                        s.data,
                        SUM(r.nota) AS nota_total
                    FROM Soluciona_avaliacao s
                    INNER JOIN Aluno al ON al.ID=s.ID_aluno
                    INNER JOIN Matricula_turma mt ON mt.matricula=al.matricula
                    INNER JOIN Resposta r
                        ON r.ID_aluno=s.ID_aluno
                       AND r.codigo_avaliacao=s.codigo_avaliacao
                    WHERE s.data BETWEEN ? AND ?
                """+filtroTurma+"""
                    GROUP BY s.ID_aluno, s.codigo_avaliacao, s.data
                ) notas_aluno ON notas_aluno.codigo_avaliacao=av.codigo
                """+filtroAvaliacao+"""
                GROUP BY av.codigo, av.titulo
                HAVING COUNT(notas_aluno.id_aluno)>=?
                ORDER BY media_avaliacao DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("codigo", rs.getInt("codigo"));
            m.put("titulo", rs.getString("titulo"));
            m.put("qtdRespondentes", rs.getInt("qtd_respondentes"));
            m.put("mediaAvaliacao", rs.getBigDecimal("media_avaliacao"));
            m.put("menorNota", rs.getBigDecimal("menor_nota"));
            m.put("maiorNota", rs.getBigDecimal("maior_nota"));
            m.put("mediaGeralSistema", rs.getBigDecimal("media_geral_sistema"));
            return m;
        }, params.toArray());
    }

    public List<Map<String, Object>>relatorioRankingQuestoes(int limite, String termo){
        String sql="""
                SELECT
                    q.codigo,
                    q.enunciado,
                    COUNT(r.ID_aluno) AS total_respostas,
                    ROUND(AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)*100.0), 1) AS percentual_acerto,
                    ROUND(100.0-AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)*100.0), 1) AS percentual_erro,
                    RANK() OVER (ORDER BY AVG(COALESCE(r.nota/NULLIF(ia.valor, 0), 0)) ASC) AS ranking_dificuldade
                FROM Questao q
                INNER JOIN Item_avaliacao ia ON ia.codigo_questao=q.codigo
                INNER JOIN Resposta r
                    ON r.codigo_avaliacao=ia.codigo_avaliacao
                   AND r.codigo_questao=ia.codigo_questao
                WHERE LOWER(q.enunciado) LIKE LOWER(?)
                GROUP BY q.codigo, q.enunciado
                ORDER BY ranking_dificuldade ASC, total_respostas DESC
                FETCH FIRST ? ROWS ONLY
                """;
        String padrao="%"+(termo!=null?termo.trim():"")+"%";
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("ranking", rs.getInt("ranking_dificuldade"));
            m.put("codigo", rs.getInt("codigo"));
            m.put("enunciado", rs.getString("enunciado"));
            m.put("totalRespostas", rs.getInt("total_respostas"));
            m.put("percentualAcerto", rs.getBigDecimal("percentual_acerto"));
            m.put("percentualErro", rs.getBigDecimal("percentual_erro"));
            return m;
        }, padrao, limite);
    }

    public List<Map<String, Object>>relatorioEvolucaoAluno(int idAluno){
        String sql="""
                SELECT
                    s.data,
                    av.codigo,
                    av.titulo,
                    ROUND(SUM(r.nota), 2) AS nota_selecionada,
                    ROUND((
                        SELECT AVG(outros.total_colega)
                        FROM (
                            SELECT SUM(r2.nota) AS total_colega
                            FROM Resposta r2
                            WHERE r2.codigo_avaliacao=av.codigo
                            GROUP BY r2.ID_aluno
                        ) outros
                    ), 2) AS media_referencia
                FROM Soluciona_avaliacao s
                INNER JOIN Avaliacao av ON av.codigo=s.codigo_avaliacao
                INNER JOIN Resposta r
                    ON r.ID_aluno=s.ID_aluno
                   AND r.codigo_avaliacao=s.codigo_avaliacao
                WHERE s.ID_aluno=?
                GROUP BY s.data, av.codigo, av.titulo
                ORDER BY s.data ASC
                """;
        return mapearEvolucao(sql, idAluno);
    }

    public List<Map<String, Object>>relatorioEvolucaoTurma(int codigoTurma){
        String sql="""
                SELECT
                    por_aluno.data,
                    av.codigo,
                    av.titulo,
                    ROUND(AVG(por_aluno.nota_total), 2) AS nota_selecionada,
                    ROUND((
                        SELECT AVG(outros.total_colega)
                        FROM (
                            SELECT SUM(r2.nota) AS total_colega
                            FROM Resposta r2
                            WHERE r2.codigo_avaliacao=av.codigo
                            GROUP BY r2.ID_aluno
                        ) outros
                    ), 2) AS media_referencia
                FROM (
                    SELECT s.ID_aluno, s.codigo_avaliacao, s.data, SUM(r.nota) AS nota_total
                    FROM Soluciona_avaliacao s
                    INNER JOIN Aluno al ON al.ID=s.ID_aluno
                    INNER JOIN Matricula_turma mt ON mt.matricula=al.matricula
                    INNER JOIN Resposta r ON r.ID_aluno=s.ID_aluno AND r.codigo_avaliacao=s.codigo_avaliacao
                    WHERE mt.codigo_turma=?
                    GROUP BY s.ID_aluno, s.codigo_avaliacao, s.data
                ) por_aluno
                INNER JOIN Avaliacao av ON av.codigo=por_aluno.codigo_avaliacao
                GROUP BY por_aluno.data, av.codigo, av.titulo
                ORDER BY por_aluno.data ASC, av.codigo ASC
                """;
        return mapearEvolucao(sql, codigoTurma);
    }

    public List<Map<String, Object>>relatorioEvolucaoGeral(){
        String sql="""
                SELECT
                    por_aluno.data,
                    av.codigo,
                    av.titulo,
                    ROUND(AVG(por_aluno.nota_total), 2) AS nota_selecionada,
                    ROUND((
                        SELECT AVG(global_sub.total_global)
                        FROM (
                            SELECT SUM(r2.nota) AS total_global
                            FROM Resposta r2
                            GROUP BY r2.ID_aluno, r2.codigo_avaliacao
                        ) global_sub
                    ), 2) AS media_referencia
                FROM (
                    SELECT s.ID_aluno, s.codigo_avaliacao, s.data, SUM(r.nota) AS nota_total
                    FROM Soluciona_avaliacao s
                    INNER JOIN Resposta r ON r.ID_aluno=s.ID_aluno AND r.codigo_avaliacao=s.codigo_avaliacao
                    GROUP BY s.ID_aluno, s.codigo_avaliacao, s.data
                ) por_aluno
                INNER JOIN Avaliacao av ON av.codigo=por_aluno.codigo_avaliacao
                GROUP BY por_aluno.data, av.codigo, av.titulo
                ORDER BY por_aluno.data ASC, av.codigo ASC
                """;
        return mapearEvolucao(sql);
    }

    private List<Map<String, Object>>mapearEvolucao(String sql, Object... params){
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("data", rs.getDate("data").toString());
            m.put("codigo", rs.getInt("codigo"));
            m.put("titulo", rs.getString("titulo"));
            m.put("notaAluno", rs.getBigDecimal("nota_selecionada"));
            m.put("mediaAvaliacao", rs.getBigDecimal("media_referencia"));
            return m;
        }, params);
    }

    public List<Map<String, Object>>relatorioPendenciasResumo(List<Integer>codigosTurma){
        if(codigosTurma==null||codigosTurma.isEmpty()){
            return List.of();
        }
        String placeholders=String.join(", ", Collections.nCopies(codigosTurma.size(), "?"));
        String sql="""
                SELECT
                    av.codigo,
                    av.titulo,
                    t.codigo AS codigo_turma,
                    t.nome AS turma,
                    COUNT(al.ID) AS total_destinados,
                    COUNT(s.ID_aluno) AS responderam,
                    COUNT(al.ID)-COUNT(s.ID_aluno) AS pendentes
                FROM Avaliacao av
                INNER JOIN Destinada d ON d.codigo_avaliacao=av.codigo
                INNER JOIN Turma t ON t.codigo=d.codigo_turma
                INNER JOIN Matricula_turma mt ON mt.codigo_turma=t.codigo
                INNER JOIN Aluno al ON al.matricula=mt.matricula
                LEFT JOIN Soluciona_avaliacao s
                    ON s.ID_aluno=al.ID
                   AND s.codigo_avaliacao=av.codigo
                WHERE t.codigo IN ("""+placeholders+"""
                )
                GROUP BY av.codigo, av.titulo, t.codigo, t.nome
                ORDER BY av.codigo, t.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("codigo", rs.getInt("codigo"));
            m.put("titulo", rs.getString("titulo"));
            m.put("codigoTurma", rs.getInt("codigo_turma"));
            m.put("turma", rs.getString("turma"));
            m.put("totalDestinados", rs.getInt("total_destinados"));
            m.put("responderam", rs.getInt("responderam"));
            m.put("pendentes", rs.getInt("pendentes"));
            return m;
        }, codigosTurma.toArray());
    }

    public List<Map<String, Object>>relatorioPendenciasNominal(List<Integer>codigosTurma){
        if(codigosTurma==null||codigosTurma.isEmpty()){
            return List.of();
        }
        String placeholders=String.join(", ", Collections.nCopies(codigosTurma.size(), "?"));
        String sql="""
                SELECT
                    av.codigo AS codigo_avaliacao,
                    av.titulo AS avaliacao,
                    t.codigo AS codigo_turma,
                    t.nome AS turma,
                    al.ID AS id_aluno,
                    al.nome AS aluno,
                    al.email
                FROM Avaliacao av
                INNER JOIN Destinada d ON d.codigo_avaliacao=av.codigo
                INNER JOIN Turma t ON t.codigo=d.codigo_turma
                INNER JOIN Matricula_turma mt ON mt.codigo_turma=t.codigo
                INNER JOIN Aluno al ON al.matricula=mt.matricula
                LEFT JOIN Soluciona_avaliacao s
                    ON s.ID_aluno=al.ID
                   AND s.codigo_avaliacao=av.codigo
                WHERE t.codigo IN ("""+placeholders+"""
                )
                  AND s.ID_aluno IS NULL
                ORDER BY av.codigo, t.nome, al.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("codigoAvaliacao", rs.getInt("codigo_avaliacao"));
            m.put("avaliacao", rs.getString("avaliacao"));
            m.put("codigoTurma", rs.getInt("codigo_turma"));
            m.put("turma", rs.getString("turma"));
            m.put("idAluno", rs.getInt("id_aluno"));
            m.put("aluno", rs.getString("aluno"));
            m.put("email", rs.getString("email"));
            return m;
        }, codigosTurma.toArray());
    }

    public List<Map<String, Object>>relatorioQuestoesSemUso(){
        String sql="""
                SELECT
                    q.codigo,
                    CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo,
                    q.enunciado
                FROM Questao q
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                LEFT JOIN Item_avaliacao ia ON ia.codigo_questao=q.codigo
                WHERE ia.codigo_avaliacao IS NULL
                ORDER BY q.codigo
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("codigo", rs.getInt("codigo"));
            m.put("tipo", rs.getString("tipo"));
            m.put("enunciado", rs.getString("enunciado"));
            return m;
        });
    }
}
