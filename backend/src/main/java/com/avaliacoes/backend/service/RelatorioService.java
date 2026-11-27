package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.RelatorioDAO;
import com.avaliacoes.backend.dao.TurmaDAO;
import com.avaliacoes.backend.model.Turma;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService{
    private final RelatorioDAO relatorioDAO;
    private final TurmaDAO turmaDAO;

    public RelatorioService(RelatorioDAO relatorioDAO, TurmaDAO turmaDAO){
        this.relatorioDAO=relatorioDAO;
        this.turmaDAO=turmaDAO;
    }

    public List<Map<String, Object>>listarAlunos(){
        return relatorioDAO.listarAlunos();
    }

    public List<Map<String, Object>>desempenhoAvaliacoes(String inicio, String fim, Integer codigoAvaliacao, Integer codigoTurma, Integer minRespondentes){
        LocalDate dtIni=(inicio!=null&&!inicio.isBlank())?LocalDate.parse(inicio):LocalDate.of(2000, 1, 1);
        LocalDate dtFim=(fim!=null&&!fim.isBlank())?LocalDate.parse(fim):LocalDate.of(2099, 12, 31);
        if(dtFim.isBefore(dtIni)){
            throw new IllegalArgumentException("A data final nao pode ser anterior a data inicial.");
        }
        int min=(minRespondentes!=null&&minRespondentes>=1)?minRespondentes:1;
        return relatorioDAO.relatorioDesempenhoAvaliacoes(dtIni, dtFim, codigoAvaliacao, codigoTurma, min);
    }

    public List<Map<String, Object>>rankingQuestoes(Integer limite, String termo){
        int lim=(limite!=null&&limite>=1&&limite<=50)?limite:10;
        return relatorioDAO.relatorioRankingQuestoes(lim, termo);
    }

    public List<Map<String, Object>>evolucao(Integer codigoTurma, Integer idAluno){
        if(idAluno!=null&&idAluno>0){
            return relatorioDAO.relatorioEvolucaoAluno(idAluno);
        }
        if(codigoTurma!=null&&codigoTurma>0){
            return relatorioDAO.relatorioEvolucaoTurma(codigoTurma);
        }
        return relatorioDAO.relatorioEvolucaoGeral();
    }

    public List<Map<String, Object>>evolucaoAluno(int idAluno){
        return evolucao(null, idAluno);
    }

    public Map<String, Object>pendenciasEQuestoesSemUso(List<Integer>turmas){
        List<Integer>ids=turmas;
        if(ids==null||ids.isEmpty()){
            ids=turmaDAO.listarTodas().stream().map(Turma::getCodigo).toList();
        }
        Map<String, Object>res=new LinkedHashMap<>();
        res.put("resumo", relatorioDAO.relatorioPendenciasResumo(ids));
        res.put("pendentes", relatorioDAO.relatorioPendenciasNominal(ids));
        res.put("questoesSemUso", relatorioDAO.relatorioQuestoesSemUso());
        return res;
    }
}
