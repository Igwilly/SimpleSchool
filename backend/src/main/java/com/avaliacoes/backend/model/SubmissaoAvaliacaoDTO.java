package com.avaliacoes.backend.model;

import java.util.ArrayList;
import java.util.List;

public class SubmissaoAvaliacaoDTO{
    private int idAluno;
    private int codigoAvaliacao;
    private String horaInicio;
    private List<RespostaSubmissaoItemDTO>respostas=new ArrayList<>();

    public int getIdAluno(){
        return idAluno;
    }

    public void setIdAluno(int idAluno){
        this.idAluno=idAluno;
    }

    public int getCodigoAvaliacao(){
        return codigoAvaliacao;
    }

    public void setCodigoAvaliacao(int codigoAvaliacao){
        this.codigoAvaliacao=codigoAvaliacao;
    }

    public String getHoraInicio(){
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio){
        this.horaInicio=horaInicio;
    }

    public List<RespostaSubmissaoItemDTO>getRespostas(){
        return respostas;
    }

    public void setRespostas(List<RespostaSubmissaoItemDTO>respostas){
        this.respostas=respostas;
    }
}
