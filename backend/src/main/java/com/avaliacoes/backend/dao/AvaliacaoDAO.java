package com.avaliacoes.backend.dao;

import com.avaliacoes.backend.model.AvaliacaoDTO;
import com.avaliacoes.backend.model.ItemAvaliacaoDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AvaliacaoDAO{
    private final JdbcTemplate jdbcTemplate;

    public AvaliacaoDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate=jdbcTemplate;
    }

    @Transactional
    public AvaliacaoDTO inserir(AvaliacaoDTO dto){
        if(dto.getCodigo()<=0){
            Integer proximo=jdbcTemplate.queryForObject("SELECT COALESCE(MAX(codigo), 0)+1 FROM Avaliacao", Integer.class);
            dto.setCodigo(proximo!=null?proximo:1);
        }

        LocalDate dt=LocalDate.parse(dto.getData());
        LocalTime hIni=LocalTime.parse(dto.getHoraInicio().length()==5?dto.getHoraInicio()+":00":dto.getHoraInicio());
        LocalTime hFim=LocalTime.parse(dto.getHoraFim().length()==5?dto.getHoraFim()+":00":dto.getHoraFim());

        jdbcTemplate.update(
                "INSERT INTO Avaliacao (codigo, titulo, descricao, materia, data, hora_inicio, hora_fim) VALUES (?, ?, ?, ?, ?, ?, ?)",
                dto.getCodigo(), dto.getTitulo(), dto.getDescricao(), dto.getMateria(),
                Date.valueOf(dt), Time.valueOf(hIni), Time.valueOf(hFim)
        );

        if(dto.getIdProfessor()>0){
            jdbcTemplate.update(
                    "INSERT INTO Elabora (ID, codigo_avaliacao) VALUES (?, ?)",
                    dto.getIdProfessor(), dto.getCodigo()
            );
        }

        if(dto.getItens()!=null){
            for(ItemAvaliacaoDTO item:dto.getItens()){
                item.setCodigoAvaliacao(dto.getCodigo());
                jdbcTemplate.update(
                        "INSERT INTO Item_avaliacao (codigo_avaliacao, codigo_questao, valor) VALUES (?, ?, ?)",
                        dto.getCodigo(), item.getCodigoQuestao(), item.getValor()
                );
            }
        }

        if(dto.getTurmasIds()!=null){
            for(Integer codigoTurma:dto.getTurmasIds()){
                jdbcTemplate.update(
                        "INSERT INTO Destinada (codigo_turma, codigo_avaliacao) VALUES (?, ?) ON CONFLICT DO NOTHING",
                        codigoTurma, dto.getCodigo()
                );
            }
        }

        return buscarPorCodigo(dto.getCodigo()).orElse(dto);
    }

    @Transactional
    public void adicionarItem(int codigoAvaliacao, ItemAvaliacaoDTO item){
        jdbcTemplate.update(
                """
                INSERT INTO Item_avaliacao (codigo_avaliacao, codigo_questao, valor)
                VALUES (?, ?, ?)
                ON CONFLICT (codigo_avaliacao, codigo_questao) DO UPDATE SET valor=EXCLUDED.valor
                """,
                codigoAvaliacao, item.getCodigoQuestao(), item.getValor()
        );
    }

    @Transactional
    public int removerItem(int codigoAvaliacao, int codigoQuestao){
        return jdbcTemplate.update(
                "DELETE FROM Item_avaliacao WHERE codigo_avaliacao=? AND codigo_questao=?",
                codigoAvaliacao, codigoQuestao
        );
    }

    public BigDecimal somarValoresExcetoQuestao(int codigoAvaliacao, int codigoQuestao){
        BigDecimal soma=jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(valor), 0) FROM Item_avaliacao WHERE codigo_avaliacao=? AND codigo_questao<>?",
                BigDecimal.class, codigoAvaliacao, codigoQuestao
        );
        return soma!=null?soma:BigDecimal.ZERO;
    }

    public boolean possuiRespostas(int codigoAvaliacao){
        Integer qtd=jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Soluciona_avaliacao WHERE codigo_avaliacao=?",
                Integer.class, codigoAvaliacao
        );
        return qtd!=null&&qtd>0;
    }

    @Transactional
    public void definirTurmas(int codigoAvaliacao, List<Integer>turmasIds){
        jdbcTemplate.update("DELETE FROM Destinada WHERE codigo_avaliacao=?", codigoAvaliacao);
        for(Integer codigoTurma:turmasIds){
            jdbcTemplate.update(
                    "INSERT INTO Destinada (codigo_turma, codigo_avaliacao) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    codigoTurma, codigoAvaliacao
            );
        }
    }

    public List<AvaliacaoDTO>listarTodas(){
        Map<Integer, AvaliacaoDTO>mapa=carregarBase(null, null);
        preencherDetalhes(mapa);
        return List.copyOf(mapa.values());
    }

    public List<AvaliacaoDTO>listarPorTurma(int codigoTurma){
        Map<Integer, AvaliacaoDTO>mapa=carregarBase(null, codigoTurma);
        preencherDetalhes(mapa);
        return List.copyOf(mapa.values());
    }

    public Optional<AvaliacaoDTO>buscarPorCodigo(int codigo){
        Map<Integer, AvaliacaoDTO>mapa=carregarBase(codigo, null);
        if(mapa.isEmpty()){
            return Optional.empty();
        }
        preencherDetalhes(mapa);
        return Optional.of(mapa.get(codigo));
    }

    private Map<Integer, AvaliacaoDTO>carregarBase(Integer filtroCodigo, Integer filtroTurma){
        String where="";
        if(filtroCodigo!=null){
            where="WHERE av.codigo=? ";
        }else if(filtroTurma!=null){
            where="WHERE EXISTS (SELECT 1 FROM Destinada d2 WHERE d2.codigo_avaliacao=av.codigo AND d2.codigo_turma=?) ";
        }
        String sql="""
                SELECT av.codigo, av.titulo, av.descricao, av.materia, av.data, av.hora_inicio, av.hora_fim,
                       MIN(p.ID) AS id_professor,
                       STRING_AGG(p.nome, ', ' ORDER BY p.nome) AS nome_professor
                FROM Avaliacao av
                LEFT JOIN Elabora e ON e.codigo_avaliacao=av.codigo
                LEFT JOIN Professor p ON p.ID=e.ID
                """+where+"""
                GROUP BY av.codigo, av.titulo, av.descricao, av.materia, av.data, av.hora_inicio, av.hora_fim
                ORDER BY av.codigo DESC
                """;

        Map<Integer, AvaliacaoDTO>mapa=new LinkedHashMap<>();
        if(filtroCodigo!=null){
            jdbcTemplate.query(sql, rs->{
                AvaliacaoDTO dto=mapearAvaliacao(rs);
                mapa.put(dto.getCodigo(), dto);
            }, filtroCodigo);
        }else if(filtroTurma!=null){
            jdbcTemplate.query(sql, rs->{
                AvaliacaoDTO dto=mapearAvaliacao(rs);
                mapa.put(dto.getCodigo(), dto);
            }, filtroTurma);
        }else{
            jdbcTemplate.query(sql, rs->{
                AvaliacaoDTO dto=mapearAvaliacao(rs);
                mapa.put(dto.getCodigo(), dto);
            });
        }
        return mapa;
    }

    private AvaliacaoDTO mapearAvaliacao(java.sql.ResultSet rs) throws java.sql.SQLException{
        AvaliacaoDTO dto=new AvaliacaoDTO();
        dto.setCodigo(rs.getInt("codigo"));
        dto.setTitulo(rs.getString("titulo"));
        dto.setDescricao(rs.getString("descricao"));
        dto.setMateria(rs.getString("materia"));
        Date dt=rs.getDate("data");
        Time hIni=rs.getTime("hora_inicio");
        Time hFim=rs.getTime("hora_fim");
        dto.setData(dt!=null?dt.toString():null);
        dto.setHoraInicio(hIni!=null?hIni.toString().substring(0, 5):null);
        dto.setHoraFim(hFim!=null?hFim.toString().substring(0, 5):null);
        dto.setIdProfessor(rs.getInt("id_professor"));
        dto.setNomeProfessor(rs.getString("nome_professor"));
        dto.setValorTotal(BigDecimal.ZERO);
        return dto;
    }

    private void preencherDetalhes(Map<Integer, AvaliacaoDTO>mapa){
        if(mapa.isEmpty()){
            return;
        }

        String sqlItens="""
                SELECT ia.codigo_avaliacao, ia.codigo_questao, ia.valor, q.enunciado,
                       CASE WHEN qo.codigo IS NOT NULL THEN 'OBJETIVA' ELSE 'DISSERTATIVA' END AS tipo
                FROM Item_avaliacao ia
                JOIN Questao q ON q.codigo=ia.codigo_questao
                LEFT JOIN Questao_objetiva qo ON qo.codigo=q.codigo
                ORDER BY ia.codigo_avaliacao, ia.codigo_questao
                """;
        jdbcTemplate.query(sqlItens, rs->{
            AvaliacaoDTO pai=mapa.get(rs.getInt("codigo_avaliacao"));
            if(pai!=null){
                ItemAvaliacaoDTO item=new ItemAvaliacaoDTO();
                item.setCodigoAvaliacao(pai.getCodigo());
                item.setCodigoQuestao(rs.getInt("codigo_questao"));
                item.setValor(rs.getBigDecimal("valor"));
                item.setEnunciado(rs.getString("enunciado"));
                item.setTipo(rs.getString("tipo"));
                pai.getItens().add(item);
                pai.setValorTotal(pai.getValorTotal().add(item.getValor()));
            }
        });

        String sqlTurmas="""
                SELECT d.codigo_avaliacao, t.codigo, t.nome
                FROM Destinada d
                JOIN Turma t ON t.codigo=d.codigo_turma
                ORDER BY d.codigo_avaliacao, t.nome
                """;
        jdbcTemplate.query(sqlTurmas, rs->{
            AvaliacaoDTO pai=mapa.get(rs.getInt("codigo_avaliacao"));
            if(pai!=null){
                pai.getTurmasIds().add(rs.getInt("codigo"));
                pai.getTurmasNomes().add(rs.getString("nome"));
            }
        });
    }
}
