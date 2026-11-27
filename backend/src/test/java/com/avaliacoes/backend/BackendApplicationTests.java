package com.avaliacoes.backend;

import com.avaliacoes.backend.dao.AuthDAO;
import com.avaliacoes.backend.dao.AvaliacaoDAO;
import com.avaliacoes.backend.dao.QuestaoDAO;
import com.avaliacoes.backend.dao.TurmaDAO;
import com.avaliacoes.backend.model.*;
import com.avaliacoes.backend.service.RealizacaoService;
import com.avaliacoes.backend.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BackendApplicationTests{
    @Autowired
    private TurmaDAO turmaDAO;

    @Autowired
    private AuthDAO authDAO;

    @Autowired
    private QuestaoDAO questaoDAO;

    @Autowired
    private AvaliacaoDAO avaliacaoDAO;

    @Autowired
    private RealizacaoService realizacaoService;

    @Autowired
    private RelatorioService relatorioService;

    @Test
    void deveTestarCrudTurma(){
        List<Turma>iniciais=turmaDAO.listarTodas();
        assertFalse(iniciais.isEmpty());

        Turma nova=new Turma();
        nova.setCodigo(999);
        nova.setNome("CC-TESTE");
        nova.setAnoLetivo(2026);
        turmaDAO.inserir(nova);

        assertTrue(turmaDAO.buscarPorCodigo(999).isPresent());

        nova.setNome("CC-ATUALIZADA");
        assertEquals(1, turmaDAO.atualizar(nova));

        assertEquals(1, turmaDAO.excluir(999));
        assertTrue(turmaDAO.buscarPorCodigo(999).isEmpty());
    }

    @Test
    void deveTestarFluxoCompletoAvaliacaoERealizacao(){
        var prof=authDAO.autenticar("geralt.rivia@gov.br", "123456");
        assertTrue(prof.isPresent());
        assertEquals("PROFESSOR", prof.get().getTipo());

        QuestaoDTO obj=new QuestaoDTO();
        obj.setTipo("OBJETIVA");
        obj.setEnunciado("Qual comando remove uma tabela no SQL?");
        Alternativa a1=new Alternativa();
        a1.setLetra("A");
        a1.setTexto("DROP TABLE");
        a1.setCorreta(true);
        Alternativa a2=new Alternativa();
        a2.setLetra("B");
        a2.setTexto("DELETE ROW");
        a2.setCorreta(false);
        obj.setAlternativas(List.of(a1, a2));
        QuestaoDTO objSalva=questaoDAO.inserir(obj);

        QuestaoDTO diss=new QuestaoDTO();
        diss.setTipo("DISSERTATIVA");
        diss.setEnunciado("Explique o uso de PreparedStatement.");
        diss.setRespostaEsperada("Previne SQL injection e parametriza consultas.");
        QuestaoDTO dissSalva=questaoDAO.inserir(diss);

        AvaliacaoDTO av=new AvaliacaoDTO();
        av.setTitulo("Quiz de revisao");
        av.setMateria("Banco de Dados I");
        av.setDescricao("Avaliacao criada no teste automatizado.");
        av.setData("2026-11-27");
        av.setHoraInicio("19:15");
        av.setHoraFim("21:00");
        av.setIdProfessor(prof.get().getId());

        ItemAvaliacaoDTO item1=new ItemAvaliacaoDTO();
        item1.setCodigoQuestao(objSalva.getCodigo());
        item1.setValor(new BigDecimal("4.00"));
        ItemAvaliacaoDTO item2=new ItemAvaliacaoDTO();
        item2.setCodigoQuestao(dissSalva.getCodigo());
        item2.setValor(new BigDecimal("6.00"));
        av.setItens(List.of(item1, item2));
        av.setTurmasIds(List.of(101));

        AvaliacaoDTO avSalva=avaliacaoDAO.inserir(av);
        assertEquals(new BigDecimal("10.00"), avSalva.getValorTotal());

        SubmissaoAvaliacaoDTO sub=new SubmissaoAvaliacaoDTO();
        sub.setIdAluno(1);
        sub.setCodigoAvaliacao(avSalva.getCodigo());
        sub.setHoraInicio("10:00:00");

        RespostaSubmissaoItemDTO r1=new RespostaSubmissaoItemDTO();
        r1.setCodigoQuestao(objSalva.getCodigo());
        r1.setLetraAlternativa("A");

        RespostaSubmissaoItemDTO r2=new RespostaSubmissaoItemDTO();
        r2.setCodigoQuestao(dissSalva.getCodigo());
        r2.setDissertacao("Usa parametros ? evitando concatenacao de SQL.");

        sub.setRespostas(List.of(r1, r2));
        ResultadoAvaliacaoDTO resultadoInicial=realizacaoService.submeter(sub);
        assertEquals(new BigDecimal("4.00"), resultadoInicial.getNotaTotal());
        assertEquals(1, resultadoInicial.getPendentesCorrecao());

        CorrecaoDissertativaDTO corr=new CorrecaoDissertativaDTO();
        corr.setIdAluno(1);
        corr.setCodigoAvaliacao(avSalva.getCodigo());
        corr.setCodigoQuestao(dissSalva.getCodigo());
        corr.setNota(new BigDecimal("5.50"));
        corr.setIdProfessor(prof.get().getId());
        realizacaoService.corrigirDissertativa(corr);

        ResultadoAvaliacaoDTO resultadoFinal=realizacaoService.buscarResultadoAluno(1, avSalva.getCodigo()).orElseThrow();
        assertEquals(new BigDecimal("9.50"), resultadoFinal.getNotaTotal());
        assertEquals(0, resultadoFinal.getPendentesCorrecao());

        assertFalse(relatorioService.desempenhoAvaliacoes("2026-01-01", "2026-12-31", null, null, 1).isEmpty());
        assertFalse(relatorioService.rankingQuestoes(10, "").isEmpty());
        assertFalse(relatorioService.evolucao(null, null).isEmpty());
        assertFalse(relatorioService.evolucao(101, null).isEmpty());
        assertFalse(relatorioService.evolucaoAluno(2).isEmpty());
        assertNotNull(relatorioService.pendenciasEQuestoesSemUso(List.of(101, 102, 103)).get("resumo"));
    }
}
