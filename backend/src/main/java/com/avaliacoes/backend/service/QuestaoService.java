package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.QuestaoDAO;
import com.avaliacoes.backend.model.Alternativa;
import com.avaliacoes.backend.model.QuestaoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class QuestaoService{
    private static final Set<String>LETRAS_VALIDAS=Set.of("A", "B", "C", "D", "E");
    private final QuestaoDAO questaoDAO;

    public QuestaoService(QuestaoDAO questaoDAO){
        this.questaoDAO=questaoDAO;
    }

    public QuestaoDTO inserir(QuestaoDTO dto){
        if(dto.getEnunciado()==null||dto.getEnunciado().isBlank()){
            throw new IllegalArgumentException("O enunciado da questao e obrigatorio.");
        }
        if("OBJETIVA".equalsIgnoreCase(dto.getTipo())){
            if(dto.getAlternativas()==null||dto.getAlternativas().size()<2){
                throw new IllegalArgumentException("Questao objetiva precisa de pelo menos 2 alternativas.");
            }
            boolean temCorreta=false;
            for(Alternativa alt:dto.getAlternativas()){
                if(alt.getLetra()==null||!LETRAS_VALIDAS.contains(alt.getLetra().trim().toUpperCase())){
                    throw new IllegalArgumentException("As letras das alternativas devem ser A, B, C, D ou E.");
                }
                if(alt.getTexto()==null||alt.getTexto().isBlank()){
                    throw new IllegalArgumentException("Todas as alternativas devem ter texto preenchido.");
                }
                if(alt.isCorreta()){
                    temCorreta=true;
                }
            }
            if(!temCorreta){
                throw new IllegalArgumentException("Marque pelo menos uma alternativa correta.");
            }
        }else{
            if(dto.getRespostaEsperada()==null||dto.getRespostaEsperada().isBlank()){
                throw new IllegalArgumentException("Informe a resposta esperada para a questao dissertativa.");
            }
        }
        return questaoDAO.inserir(dto);
    }

    public List<QuestaoDTO>listarTodas(){
        return questaoDAO.listarTodas();
    }

    public Optional<QuestaoDTO>buscarPorCodigo(int codigo){
        return questaoDAO.buscarPorCodigo(codigo);
    }

    public int excluir(int codigo){
        return questaoDAO.excluir(codigo);
    }
}
