package com.avaliacoes.backend;

import com.avaliacoes.backend.dao.AuthDAO;
import com.avaliacoes.backend.dao.AvaliacaoDAO;
import com.avaliacoes.backend.dao.QuestaoDAO;
import com.avaliacoes.backend.dao.TurmaDAO;
import com.avaliacoes.backend.model.*;
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
    void deveTestarLoginQuestoesEAvaliacoes(){
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
        assertTrue(objSalva.getCodigo()>0);

        QuestaoDTO diss=new QuestaoDTO();
        diss.setTipo("DISSERTATIVA");
        diss.setEnunciado("Explique o uso de PreparedStatement.");
        diss.setRespostaEsperada("Previne SQL injection e parametriza consultas.");
        QuestaoDTO dissSalva=questaoDAO.inserir(diss);
        assertTrue(dissSalva.getCodigo()>0);

        AvaliacaoDTO av=new AvaliacaoDTO();
        av.setTitulo("Quiz de revisao");
        av.setMateria("Banco de Dados I");
        av.setDescricao("Avaliacao criada no teste automatizado.");
        av.setIdProfessor(prof.get().getId());

        ItemAvaliacaoDTO item1=new ItemAvaliacaoDTO();
        item1.setCodigoQuestao(objSalva.getCodigo());
        item1.setValor(new BigDecimal("5.00"));
        ItemAvaliacaoDTO item2=new ItemAvaliacaoDTO();
        item2.setCodigoQuestao(dissSalva.getCodigo());
        item2.setValor(new BigDecimal("5.00"));
        av.setItens(List.of(item1, item2));
        av.setTurmasIds(List.of(101, 102));

        AvaliacaoDTO avSalva=avaliacaoDAO.inserir(av);
        assertEquals(2, avSalva.getItens().size());
        assertEquals(2, avSalva.getTurmasIds().size());
        assertEquals(new BigDecimal("10.00"), avSalva.getValorTotal());
    }
}
