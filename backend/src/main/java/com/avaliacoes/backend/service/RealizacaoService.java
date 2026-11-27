package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.RealizacaoDAO;
import com.avaliacoes.backend.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RealizacaoService{
    private final RealizacaoDAO realizacaoDAO;

    public RealizacaoService(RealizacaoDAO realizacaoDAO){
        this.realizacaoDAO=realizacaoDAO;
    }

    public List<AvaliacaoAlunoResumoDTO>listarAvaliacoesDoAluno(int idAluno){
        return realizacaoDAO.listarAvaliacoesDoAluno(idAluno);
    }

    public Optional<Map<String, Object>>carregarProvaParaResponder(int idAluno, int codigoAvaliacao){
        if(!realizacaoDAO.avaliacaoDestinadaAoAluno(idAluno, codigoAvaliacao)){
            throw new IllegalArgumentException("Esta avaliacao nao e destinada a sua turma.");
        }
        if(realizacaoDAO.jaRespondeu(idAluno, codigoAvaliacao)){
            throw new IllegalArgumentException("Voce ja respondeu esta avaliacao.");
        }
        return realizacaoDAO.carregarProvaParaResponder(codigoAvaliacao);
    }

    public ResultadoAvaliacaoDTO submeter(SubmissaoAvaliacaoDTO dto){
        if(dto.getIdAluno()<=0||dto.getCodigoAvaliacao()<=0){
            throw new IllegalArgumentException("Dados da avaliacao ou aluno invalidos.");
        }
        if(!realizacaoDAO.avaliacaoDestinadaAoAluno(dto.getIdAluno(), dto.getCodigoAvaliacao())){
            throw new IllegalArgumentException("Esta avaliacao nao e destinada a sua turma.");
        }
        if(realizacaoDAO.jaRespondeu(dto.getIdAluno(), dto.getCodigoAvaliacao())){
            throw new IllegalArgumentException("Voce ja respondeu esta avaliacao.");
        }
        if(dto.getRespostas()==null||dto.getRespostas().isEmpty()){
            throw new IllegalArgumentException("Envie as respostas da avaliacao.");
        }

        LocalDate hoje=LocalDate.now();
        LocalTime horaFim=LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalTime horaIni;
        try{
            horaIni=dto.getHoraInicio()!=null&&!dto.getHoraInicio().isBlank()
                    ?LocalTime.parse(dto.getHoraInicio()).truncatedTo(ChronoUnit.SECONDS)
                    :horaFim.minusSeconds(1);
        }catch(Exception ex){
            horaIni=horaFim.minusSeconds(1);
        }

        if(!horaFim.isAfter(horaIni)){
            if(horaIni.isBefore(LocalTime.of(23, 59, 58))){
                horaFim=horaIni.plusSeconds(1);
            }else{
                horaIni=horaFim.minusSeconds(1);
            }
        }

        return realizacaoDAO.submeterRespostas(dto, hoje, horaIni, horaFim);
    }

    public Optional<ResultadoAvaliacaoDTO>buscarResultadoAluno(int idAluno, int codigoAvaliacao){
        return realizacaoDAO.buscarResultadoAluno(idAluno, codigoAvaliacao);
    }

    public List<CorrecaoDissertativaDTO>listarRespostasDissertativas(boolean apenasPendentes){
        return realizacaoDAO.listarRespostasDissertativas(apenasPendentes);
    }

    public void corrigirDissertativa(CorrecaoDissertativaDTO dto){
        if(dto.getNota()==null||dto.getNota().compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("A nota deve ser maior ou igual a zero.");
        }
        BigDecimal valorMaximo=realizacaoDAO.buscarValorItem(dto.getCodigoAvaliacao(), dto.getCodigoQuestao())
                .orElseThrow(()->new IllegalArgumentException("Item da avaliacao nao encontrado."));
        if(dto.getNota().compareTo(valorMaximo)>0){
            throw new IllegalArgumentException("A nota nao pode ultrapassar o valor da questao ("+valorMaximo+").");
        }
        int idProf=dto.getIdProfessor()>0?dto.getIdProfessor():1;
        realizacaoDAO.atribuirNotaDissertativa(
                dto.getIdAluno(), dto.getCodigoAvaliacao(), dto.getCodigoQuestao(), dto.getNota(), idProf
        );
    }

    public List<NotaAlunoAvaliacaoDTO>listarNotasTotais(Integer codigoAvaliacao){
        return realizacaoDAO.listarNotasTotaisPorAvaliacao(codigoAvaliacao);
    }
}
