package com.avaliacoes.backend.dao;

import com.avaliacoes.backend.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Repository
public class RealizacaoDAO{
    private final JdbcTemplate jdbcTemplate;

    public RealizacaoDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<AvaliacaoAlunoResumoDTO>listarAvaliacoesDoAluno(int idAluno){
        String sql="""
                SELECT av.codigo, av.titulo, av.materia, av.descricao, av.data AS data_avaliacao, av.hora_inicio, av.hora_fim,
                       COUNT(DISTINCT ia.codigo_questao) AS qtd_questoes,
                       COALESCE(SUM(ia.valor), 0) AS valor_total,
                       sa.data AS data_realizacao,
                       COALESCE((
                           SELECT SUM(r.nota)
                           FROM Resposta r
                           WHERE r.ID_aluno=al.ID AND r.codigo_avaliacao=av.codigo
                       ), 0) AS nota_obtida,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM Resposta_dissertativa rd
                           WHERE rd.ID_aluno=al.ID
                             AND rd.codigo_avaliacao=av.codigo
                             AND rd.data_correcao IS NULL
                       ), 0) AS pendentes_correcao
                FROM Aluno al
                JOIN Matricula_turma mt ON mt.matricula=al.matricula
                JOIN Destinada d ON d.codigo_turma=mt.codigo_turma
                JOIN Avaliacao av ON av.codigo=d.codigo_avaliacao
                LEFT JOIN Item_avaliacao ia ON ia.codigo_avaliacao=av.codigo
                LEFT JOIN Soluciona_avaliacao sa
                       ON sa.ID_aluno=al.ID AND sa.codigo_avaliacao=av.codigo
                WHERE al.ID=?
                GROUP BY av.codigo, av.titulo, av.materia, av.descricao, av.data, av.hora_inicio, av.hora_fim, sa.data, al.ID
                ORDER BY (sa.data IS NOT NULL) ASC, av.codigo DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            AvaliacaoAlunoResumoDTO dto=new AvaliacaoAlunoResumoDTO();
            dto.setCodigoAvaliacao(rs.getInt("codigo"));
            dto.setTitulo(rs.getString("titulo"));
            dto.setMateria(rs.getString("materia"));
            dto.setDescricao(rs.getString("descricao"));
            Date dtAv=rs.getDate("data_avaliacao");
            Time hIni=rs.getTime("hora_inicio");
            Time hFim=rs.getTime("hora_fim");
            dto.setDataAvaliacao(dtAv!=null?dtAv.toString():null);
            dto.setHoraInicio(hIni!=null?hIni.toString().substring(0, 5):null);
            dto.setHoraFim(hFim!=null?hFim.toString().substring(0, 5):null);
            dto.setQtdQuestoes(rs.getInt("qtd_questoes"));
            dto.setValorTotal(rs.getBigDecimal("valor_total"));
            Date dt=rs.getDate("data_realizacao");
            dto.setRespondida(dt!=null);
            dto.setDataRealizacao(dt!=null?dt.toString():null);
            dto.setNotaObtida(rs.getBigDecimal("nota_obtida"));
            dto.setPendentesCorrecao(rs.getInt("pendentes_correcao"));
            return dto;
        }, idAluno);
    }

    public Optional<Map<String, Object>>carregarProvaParaResponder(int codigoAvaliacao){
        String sqlAv="SELECT codigo, titulo, materia, descricao FROM Avaliacao WHERE codigo=?";
        List<Map<String, Object>>avs=jdbcTemplate.query(sqlAv, (rs, rowNum)->{
            Map<String, Object>m=new LinkedHashMap<>();
            m.put("codigo", rs.getInt("codigo"));
            m.put("titulo", rs.getString("titulo"));
            m.put("materia", rs.getString("materia"));
            m.put("descricao", rs.getString("descricao"));
            return m;
        }, codigoAvaliacao);
        if(avs.isEmpty()){
            return Optional.empty();
        }

        Map<String, Object>prova=avs.get(0);
        String sqlQuestoes="""
                SELECT ia.codigo_questao, ia.valor, q.enunciado, qo.imagem,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo
                FROM Item_avaliacao ia
                JOIN Questao q ON q.codigo=ia.codigo_questao
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                WHERE ia.codigo_avaliacao=?
                ORDER BY ia.codigo_questao
                """;

        Map<Integer, Map<String, Object>>mapaQuestoes=new LinkedHashMap<>();
        BigDecimal soma=BigDecimal.ZERO;
        List<Map<String, Object>>listaQuestoes=jdbcTemplate.query(sqlQuestoes, (rs, rowNum)->{
            Map<String, Object>q=new LinkedHashMap<>();
            int codQ=rs.getInt("codigo_questao");
            q.put("codigoQuestao", codQ);
            q.put("valor", rs.getBigDecimal("valor"));
            q.put("enunciado", rs.getString("enunciado"));
            q.put("imagem", rs.getString("imagem"));
            q.put("tipo", rs.getString("tipo"));
            q.put("alternativas", new ArrayList<Map<String, Object>>());
            mapaQuestoes.put(codQ, q);
            return q;
        }, codigoAvaliacao);

        for(Map<String, Object>q:listaQuestoes){
            soma=soma.add((BigDecimal)q.get("valor"));
        }

        if(!mapaQuestoes.isEmpty()){
            String sqlAlt="""
                    SELECT a.codigo_questao, a.letra, a.texto
                    FROM Alternativa a
                    JOIN Item_avaliacao ia ON ia.codigo_questao=a.codigo_questao
                    WHERE ia.codigo_avaliacao=?
                    ORDER BY a.codigo_questao, a.letra
                    """;
            jdbcTemplate.query(sqlAlt, rs->{
                Map<String, Object>pai=mapaQuestoes.get(rs.getInt("codigo_questao"));
                if(pai!=null){
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>>alts=(List<Map<String, Object>>)pai.get("alternativas");
                    Map<String, Object>alt=new LinkedHashMap<>();
                    alt.put("letra", rs.getString("letra").trim());
                    alt.put("texto", rs.getString("texto"));
                    alts.add(alt);
                }
            }, codigoAvaliacao);
        }

        prova.put("valorTotal", soma);
        prova.put("questoes", listaQuestoes);
        return Optional.of(prova);
    }

    public boolean jaRespondeu(int idAluno, int codigoAvaliacao){
        Integer qtd=jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Soluciona_avaliacao WHERE ID_aluno=? AND codigo_avaliacao=?",
                Integer.class, idAluno, codigoAvaliacao
        );
        return qtd!=null&&qtd>0;
    }

    public boolean avaliacaoDestinadaAoAluno(int idAluno, int codigoAvaliacao){
        Integer qtd=jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM Aluno al
                JOIN Matricula_turma mt ON mt.matricula=al.matricula
                JOIN Destinada d ON d.codigo_turma=mt.codigo_turma
                WHERE al.ID=? AND d.codigo_avaliacao=?
                """,
                Integer.class, idAluno, codigoAvaliacao
        );
        return qtd!=null&&qtd>0;
    }

    @Transactional
    public ResultadoAvaliacaoDTO submeterRespostas(SubmissaoAvaliacaoDTO dto, LocalDate dataHoje, LocalTime horaIni, LocalTime horaFim){
        jdbcTemplate.update(
                "INSERT INTO Soluciona_avaliacao (ID_aluno, codigo_avaliacao, data, hora_inicio, hora_fim) VALUES (?, ?, ?, ?, ?)",
                dto.getIdAluno(), dto.getCodigoAvaliacao(), Date.valueOf(dataHoje), Time.valueOf(horaIni), Time.valueOf(horaFim)
        );

        Integer idProfPadrao=jdbcTemplate.queryForObject(
                "SELECT COALESCE((SELECT MIN(ID) FROM Elabora WHERE codigo_avaliacao=?), (SELECT MIN(ID) FROM Professor))",
                Integer.class, dto.getCodigoAvaliacao()
        );

        String sqlItens="""
                SELECT ia.codigo_questao, ia.valor,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo
                FROM Item_avaliacao ia
                LEFT JOIN Questao_objetiva qo ON qo.codigo=ia.codigo_questao
                WHERE ia.codigo_avaliacao=?
                ORDER BY ia.codigo_questao
                """;
        List<Map<String, Object>>itens=jdbcTemplate.queryForList(sqlItens, dto.getCodigoAvaliacao());

        Map<Integer, RespostaSubmissaoItemDTO>enviadas=new HashMap<>();
        for(RespostaSubmissaoItemDTO item:dto.getRespostas()){
            enviadas.put(item.getCodigoQuestao(), item);
        }

        for(Map<String, Object>itemBanco:itens){
            int codQuestao=((Number)itemBanco.get("codigo_questao")).intValue();
            BigDecimal valor=(BigDecimal)itemBanco.get("valor");
            String tipo=(String)itemBanco.get("tipo");
            RespostaSubmissaoItemDTO resp=enviadas.get(codQuestao);
            if(resp==null){
                throw new IllegalArgumentException("Responda todas as questoes da avaliacao antes de enviar.");
            }

            if("OBJETIVA".equals(tipo)){
                if(resp.getLetraAlternativa()==null||resp.getLetraAlternativa().isBlank()){
                    throw new IllegalArgumentException("Selecione uma alternativa para a questao #"+codQuestao+".");
                }
                String letra=resp.getLetraAlternativa().trim().toUpperCase();
                List<Boolean>corretas=jdbcTemplate.query(
                        "SELECT correta FROM Alternativa WHERE codigo_questao=? AND letra=?",
                        (rs, rowNum)->rs.getBoolean("correta"),
                        codQuestao, letra
                );
                if(corretas.isEmpty()){
                    throw new IllegalArgumentException("Alternativa invalida na questao #"+codQuestao+".");
                }
                BigDecimal nota=corretas.get(0)?valor:BigDecimal.ZERO.setScale(2);

                jdbcTemplate.update(
                        "INSERT INTO Resposta (ID_aluno, codigo_avaliacao, codigo_questao, nota) VALUES (?, ?, ?, ?)",
                        dto.getIdAluno(), dto.getCodigoAvaliacao(), codQuestao, nota
                );
                jdbcTemplate.update(
                        "INSERT INTO Resposta_objetiva (ID_aluno, codigo_avaliacao, codigo_questao, data, letra_alternativa) VALUES (?, ?, ?, ?, ?)",
                        dto.getIdAluno(), dto.getCodigoAvaliacao(), codQuestao, Date.valueOf(dataHoje), letra
                );
            }else{
                if(resp.getDissertacao()==null||resp.getDissertacao().isBlank()){
                    throw new IllegalArgumentException("Preencha a resposta dissertativa da questao #"+codQuestao+".");
                }
                jdbcTemplate.update(
                        "INSERT INTO Resposta (ID_aluno, codigo_avaliacao, codigo_questao, nota) VALUES (?, ?, ?, ?)",
                        dto.getIdAluno(), dto.getCodigoAvaliacao(), codQuestao, BigDecimal.ZERO.setScale(2)
                );
                jdbcTemplate.update(
                        "INSERT INTO Resposta_dissertativa (ID_aluno, codigo_avaliacao, codigo_questao, data, dissertacao, ID_professor, data_correcao) VALUES (?, ?, ?, ?, ?, ?, NULL)",
                        dto.getIdAluno(), dto.getCodigoAvaliacao(), codQuestao, Date.valueOf(dataHoje), resp.getDissertacao().trim(), idProfPadrao
                );
            }
        }

        return buscarResultadoAluno(dto.getIdAluno(), dto.getCodigoAvaliacao()).orElseThrow();
    }

    public Optional<ResultadoAvaliacaoDTO>buscarResultadoAluno(int idAluno, int codigoAvaliacao){
        String sqlCabecalho="""
                SELECT av.codigo, av.titulo, av.materia, sa.data, sa.hora_inicio, sa.hora_fim
                FROM Soluciona_avaliacao sa
                JOIN Avaliacao av ON av.codigo=sa.codigo_avaliacao
                WHERE sa.ID_aluno=? AND sa.codigo_avaliacao=?
                ORDER BY sa.data DESC
                LIMIT 1
                """;
        List<ResultadoAvaliacaoDTO>cab=jdbcTemplate.query(sqlCabecalho, (rs, rowNum)->{
            ResultadoAvaliacaoDTO r=new ResultadoAvaliacaoDTO();
            r.setCodigoAvaliacao(rs.getInt("codigo"));
            r.setTitulo(rs.getString("titulo"));
            r.setMateria(rs.getString("materia"));
            r.setData(rs.getDate("data").toString());
            r.setHoraInicio(rs.getTime("hora_inicio").toString());
            r.setHoraFim(rs.getTime("hora_fim").toString());
            return r;
        }, idAluno, codigoAvaliacao);

        if(cab.isEmpty()){
            return Optional.empty();
        }

        ResultadoAvaliacaoDTO resultado=cab.get(0);
        String sqlQuestoes="""
                SELECT ia.codigo_questao, q.enunciado, qo.imagem, ia.valor, r.nota,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo,
                       ro.letra_alternativa,
                       (SELECT a.letra FROM Alternativa a WHERE a.codigo_questao=q.codigo AND a.correta=TRUE LIMIT 1) AS letra_correta,
                       rd.dissertacao,
                       rd.data_correcao
                FROM Item_avaliacao ia
                JOIN Questao q ON q.codigo=ia.codigo_questao
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                JOIN Resposta r ON r.ID_aluno=? AND r.codigo_avaliacao=ia.codigo_avaliacao AND r.codigo_questao=ia.codigo_questao
                LEFT JOIN Resposta_objetiva ro ON ro.ID_aluno=r.ID_aluno AND ro.codigo_avaliacao=r.codigo_avaliacao AND ro.codigo_questao=r.codigo_questao
                LEFT JOIN Resposta_dissertativa rd ON rd.ID_aluno=r.ID_aluno AND rd.codigo_avaliacao=r.codigo_avaliacao AND rd.codigo_questao=r.codigo_questao
                WHERE ia.codigo_avaliacao=?
                ORDER BY ia.codigo_questao
                """;

        BigDecimal valorTotal=BigDecimal.ZERO;
        BigDecimal notaTotal=BigDecimal.ZERO;
        int pendentes=0;

        List<QuestaoResultadoDTO>questoes=jdbcTemplate.query(sqlQuestoes, (rs, rowNum)->{
            QuestaoResultadoDTO q=new QuestaoResultadoDTO();
            q.setCodigoQuestao(rs.getInt("codigo_questao"));
            q.setEnunciado(rs.getString("enunciado"));
            q.setImagem(rs.getString("imagem"));
            q.setTipo(rs.getString("tipo"));
            q.setValor(rs.getBigDecimal("valor"));
            q.setNotaObtida(rs.getBigDecimal("nota"));
            String marcada=rs.getString("letra_alternativa");
            String correta=rs.getString("letra_correta");
            q.setLetraMarcada(marcada!=null?marcada.trim():null);
            q.setLetraCorreta(correta!=null?correta.trim():null);
            q.setDissertacao(rs.getString("dissertacao"));
            q.setCorrigida("OBJETIVA".equals(q.getTipo())||rs.getDate("data_correcao")!=null);
            return q;
        }, idAluno, codigoAvaliacao);

        for(QuestaoResultadoDTO q:questoes){
            valorTotal=valorTotal.add(q.getValor());
            notaTotal=notaTotal.add(q.getNotaObtida());
            if(!q.isCorrigida()){
                pendentes++;
            }
        }

        resultado.setValorTotal(valorTotal);
        resultado.setNotaTotal(notaTotal);
        resultado.setPendentesCorrecao(pendentes);
        resultado.setQuestoes(questoes);
        return Optional.of(resultado);
    }

    public List<CorrecaoDissertativaDTO>listarRespostasDissertativas(boolean apenasPendentes){
        String filtro=apenasPendentes?"WHERE rd.data_correcao IS NULL ":"";
        String sql="""
                SELECT rd.ID_aluno, al.nome AS nome_aluno,
                       rd.codigo_avaliacao, av.titulo AS titulo_avaliacao,
                       rd.codigo_questao, q.enunciado, qd.resposta_esperada,
                       rd.dissertacao, ia.valor AS valor_maximo, r.nota,
                       rd.data AS data_realizacao, rd.data_correcao, rd.ID_professor
                FROM Resposta_dissertativa rd
                JOIN Aluno al ON al.ID=rd.ID_aluno
                JOIN Avaliacao av ON av.codigo=rd.codigo_avaliacao
                JOIN Questao q ON q.codigo=rd.codigo_questao
                JOIN Questao_dissertativa qd ON qd.codigo=q.codigo
                JOIN Item_avaliacao ia ON ia.codigo_avaliacao=rd.codigo_avaliacao AND ia.codigo_questao=rd.codigo_questao
                JOIN Resposta r ON r.ID_aluno=rd.ID_aluno AND r.codigo_avaliacao=rd.codigo_avaliacao AND r.codigo_questao=rd.codigo_questao
                """+filtro+"""
                ORDER BY (rd.data_correcao IS NULL) DESC, rd.data DESC, rd.codigo_avaliacao, al.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum)->{
            CorrecaoDissertativaDTO c=new CorrecaoDissertativaDTO();
            c.setIdAluno(rs.getInt("ID_aluno"));
            c.setNomeAluno(rs.getString("nome_aluno"));
            c.setCodigoAvaliacao(rs.getInt("codigo_avaliacao"));
            c.setTituloAvaliacao(rs.getString("titulo_avaliacao"));
            c.setCodigoQuestao(rs.getInt("codigo_questao"));
            c.setEnunciado(rs.getString("enunciado"));
            c.setRespostaEsperada(rs.getString("resposta_esperada"));
            c.setDissertacao(rs.getString("dissertacao"));
            c.setValorMaximo(rs.getBigDecimal("valor_maximo"));
            c.setNota(rs.getBigDecimal("nota"));
            c.setDataRealizacao(rs.getDate("data_realizacao").toString());
            Date dtCorr=rs.getDate("data_correcao");
            c.setDataCorrecao(dtCorr!=null?dtCorr.toString():null);
            c.setIdProfessor(rs.getInt("ID_professor"));
            return c;
        });
    }

    public Optional<BigDecimal>buscarValorItem(int codigoAvaliacao, int codigoQuestao){
        List<BigDecimal>valores=jdbcTemplate.query(
                "SELECT valor FROM Item_avaliacao WHERE codigo_avaliacao=? AND codigo_questao=?",
                (rs, rowNum)->rs.getBigDecimal("valor"),
                codigoAvaliacao, codigoQuestao
        );
        return valores.stream().findFirst();
    }

    @Transactional
    public void atribuirNotaDissertativa(int idAluno, int codigoAvaliacao, int codigoQuestao, BigDecimal nota, int idProfessor){
        int linhasResp=jdbcTemplate.update(
                "UPDATE Resposta SET nota=? WHERE ID_aluno=? AND codigo_avaliacao=? AND codigo_questao=?",
                nota, idAluno, codigoAvaliacao, codigoQuestao
        );
        if(linhasResp==0){
            throw new IllegalArgumentException("Resposta nao encontrada.");
        }
        jdbcTemplate.update(
                "UPDATE Resposta_dissertativa SET ID_professor=?, data_correcao=GREATEST(CURRENT_DATE, data) WHERE ID_aluno=? AND codigo_avaliacao=? AND codigo_questao=?",
                idProfessor, idAluno, codigoAvaliacao, codigoQuestao
        );
    }

    public List<NotaAlunoAvaliacaoDTO>listarNotasTotaisPorAvaliacao(Integer codigoAvaliacao){
        String filtro=codigoAvaliacao!=null?"WHERE sa.codigo_avaliacao=? ":"";
        String sql="""
                SELECT al.ID AS id_aluno, al.nome AS nome_aluno, t.nome AS turma_nome,
                       av.codigo AS codigo_avaliacao, av.titulo AS titulo_avaliacao,
                       sa.data,
                       COALESCE((SELECT SUM(ia.valor) FROM Item_avaliacao ia WHERE ia.codigo_avaliacao=av.codigo), 0) AS valor_total,
                       COALESCE((SELECT SUM(r.nota) FROM Resposta r WHERE r.ID_aluno=al.ID AND r.codigo_avaliacao=av.codigo), 0) AS nota_total,
                       COALESCE((
                           SELECT COUNT(*)
                           FROM Resposta_dissertativa rd
                           WHERE rd.ID_aluno=al.ID AND rd.codigo_avaliacao=av.codigo AND rd.data_correcao IS NULL
                       ), 0) AS pendentes_correcao
                FROM Soluciona_avaliacao sa
                JOIN Aluno al ON al.ID=sa.ID_aluno
                JOIN Matricula_turma mt ON mt.matricula=al.matricula
                JOIN Turma t ON t.codigo=mt.codigo_turma
                JOIN Avaliacao av ON av.codigo=sa.codigo_avaliacao
                """+filtro+"""
                ORDER BY av.codigo DESC, nota_total DESC, al.nome
                """;
        if(codigoAvaliacao!=null){
            return jdbcTemplate.query(sql, this::mapearNotaAluno, codigoAvaliacao);
        }
        return jdbcTemplate.query(sql, this::mapearNotaAluno);
    }

    private NotaAlunoAvaliacaoDTO mapearNotaAluno(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException{
        NotaAlunoAvaliacaoDTO dto=new NotaAlunoAvaliacaoDTO();
        dto.setIdAluno(rs.getInt("id_aluno"));
        dto.setNomeAluno(rs.getString("nome_aluno"));
        dto.setTurmaNome(rs.getString("turma_nome"));
        dto.setCodigoAvaliacao(rs.getInt("codigo_avaliacao"));
        dto.setTituloAvaliacao(rs.getString("titulo_avaliacao"));
        dto.setData(rs.getDate("data").toString());
        dto.setValorTotal(rs.getBigDecimal("valor_total"));
        dto.setNotaTotal(rs.getBigDecimal("nota_total"));
        dto.setPendentesCorrecao(rs.getInt("pendentes_correcao"));
        return dto;
    }
}
