package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.AvaliacaoDAO;
import com.avaliacoes.backend.model.AvaliacaoDTO;
import com.avaliacoes.backend.model.ItemAvaliacaoDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoService{
    private final AvaliacaoDAO avaliacaoDAO;

    public AvaliacaoService(AvaliacaoDAO avaliacaoDAO){
        this.avaliacaoDAO=avaliacaoDAO;
    }

    public AvaliacaoDTO inserir(AvaliacaoDTO dto){
        if(dto.getTitulo()==null||dto.getTitulo().isBlank()){
            throw new IllegalArgumentException("O titulo da avaliacao e obrigatorio.");
        }
        if(dto.getTitulo().length()>50){
            throw new IllegalArgumentException("O titulo deve ter no maximo 50 caracteres.");
        }
        if(dto.getMateria()==null||dto.getMateria().isBlank()){
            throw new IllegalArgumentException("A materia da avaliacao e obrigatoria.");
        }
        if(dto.getItens()==null||dto.getItens().isEmpty()){
            throw new IllegalArgumentException("Selecione pelo menos uma questao para montar a avaliacao.");
        }
        for(ItemAvaliacaoDTO item:dto.getItens()){
            if(item.getValor()==null||item.getValor().compareTo(BigDecimal.ZERO)<=0){
                throw new IllegalArgumentException("Cada questao da avaliacao deve ter valor maior que zero.");
            }
        }
        if(dto.getTurmasIds()==null||dto.getTurmasIds().isEmpty()){
            throw new IllegalArgumentException("Selecione pelo menos uma turma destinataria.");
        }
        return avaliacaoDAO.inserir(dto);
    }

    public void adicionarItem(int codigoAvaliacao, ItemAvaliacaoDTO item){
        if(item.getValor()==null||item.getValor().compareTo(BigDecimal.ZERO)<=0){
            throw new IllegalArgumentException("O valor da questao deve ser maior que zero.");
        }
        avaliacaoDAO.adicionarItem(codigoAvaliacao, item);
    }

    public int removerItem(int codigoAvaliacao, int codigoQuestao){
        return avaliacaoDAO.removerItem(codigoAvaliacao, codigoQuestao);
    }

    public void definirTurmas(int codigoAvaliacao, List<Integer>turmasIds){
        if(turmasIds==null||turmasIds.isEmpty()){
            throw new IllegalArgumentException("Selecione pelo menos uma turma.");
        }
        avaliacaoDAO.definirTurmas(codigoAvaliacao, turmasIds);
    }

    public List<AvaliacaoDTO>listarTodas(){
        return avaliacaoDAO.listarTodas();
    }

    public List<AvaliacaoDTO>listarPorTurma(int codigoTurma){
        return avaliacaoDAO.listarPorTurma(codigoTurma);
    }

    public Optional<AvaliacaoDTO>buscarPorCodigo(int codigo){
        return avaliacaoDAO.buscarPorCodigo(codigo);
    }
}
