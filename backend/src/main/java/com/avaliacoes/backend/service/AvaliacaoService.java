package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.AvaliacaoDAO;
import com.avaliacoes.backend.model.AvaliacaoDTO;
import com.avaliacoes.backend.model.ItemAvaliacaoDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AvaliacaoService{
    private static final BigDecimal VALOR_MAX_PROVA=new BigDecimal("100.00");
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
        if(dto.getData()==null||dto.getData().isBlank()){
            dto.setData(LocalDate.now().toString());
        }else{
            LocalDate.parse(dto.getData());
        }
        if(dto.getHoraInicio()==null||dto.getHoraInicio().isBlank()){
            dto.setHoraInicio("19:00");
        }
        if(dto.getHoraFim()==null||dto.getHoraFim().isBlank()){
            dto.setHoraFim("21:00");
        }
        LocalTime ini=LocalTime.parse(dto.getHoraInicio().length()==5?dto.getHoraInicio()+":00":dto.getHoraInicio());
        LocalTime fim=LocalTime.parse(dto.getHoraFim().length()==5?dto.getHoraFim()+":00":dto.getHoraFim());
        if(!fim.isAfter(ini)){
            throw new IllegalArgumentException("O horario de fim deve ser maior que o horario de inicio.");
        }

        if(dto.getItens()==null||dto.getItens().isEmpty()){
            throw new IllegalArgumentException("Selecione pelo menos uma questao para montar a avaliacao.");
        }
        BigDecimal somaTotal=BigDecimal.ZERO;
        for(ItemAvaliacaoDTO item:dto.getItens()){
            if(item.getValor()==null||item.getValor().compareTo(BigDecimal.ZERO)<0){
                throw new IllegalArgumentException("O valor de cada questao nao pode ser menor que 0.");
            }
            if(item.getValor().compareTo(VALOR_MAX_PROVA)>0){
                throw new IllegalArgumentException("O valor maximo permitido na avaliacao e 100.00.");
            }
            somaTotal=somaTotal.add(item.getValor());
        }
        if(somaTotal.compareTo(VALOR_MAX_PROVA)>0){
            throw new IllegalArgumentException("O valor total da avaliacao nao pode ultrapassar 100.00 (atual: "+somaTotal+").");
        }
        if(dto.getTurmasIds()==null||dto.getTurmasIds().isEmpty()){
            throw new IllegalArgumentException("Selecione pelo menos uma turma destinataria.");
        }
        return avaliacaoDAO.inserir(dto);
    }

    public void adicionarItem(int codigoAvaliacao, ItemAvaliacaoDTO item){
        if(item.getValor()==null||item.getValor().compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("O valor da questao nao pode ser menor que 0.");
        }
        BigDecimal somaOutras=avaliacaoDAO.somarValoresExcetoQuestao(codigoAvaliacao, item.getCodigoQuestao());
        if(somaOutras.add(item.getValor()).compareTo(VALOR_MAX_PROVA)>0){
            throw new IllegalArgumentException("O valor total da avaliacao nao pode ultrapassar 100.00.");
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
