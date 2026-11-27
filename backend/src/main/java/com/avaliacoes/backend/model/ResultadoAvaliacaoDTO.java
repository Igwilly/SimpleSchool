package com.avaliacoes.backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ResultadoAvaliacaoDTO{
    private int codigoAvaliacao;
    private String titulo;
    private String materia;
    private String data;
    private String horaInicio;
    private String horaFim;
    private BigDecimal valorTotal=BigDecimal.ZERO;
    private BigDecimal notaTotal=BigDecimal.ZERO;
    private int pendentesCorrecao;
    private List<QuestaoResultadoDTO>questoes=new ArrayList<>();

    public int getCodigoAvaliacao(){
        return codigoAvaliacao;
    }

    public void setCodigoAvaliacao(int codigoAvaliacao){
        this.codigoAvaliacao=codigoAvaliacao;
    }

    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String titulo){
        this.titulo=titulo;
    }

    public String getMateria(){
        return materia;
    }

    public void setMateria(String materia){
        this.materia=materia;
    }

    public String getData(){
        return data;
    }

    public void setData(String data){
        this.data=data;
    }

    public String getHoraInicio(){
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio){
        this.horaInicio=horaInicio;
    }

    public String getHoraFim(){
        return horaFim;
    }

    public void setHoraFim(String horaFim){
        this.horaFim=horaFim;
    }

    public BigDecimal getValorTotal(){
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal){
        this.valorTotal=valorTotal;
    }

    public BigDecimal getNotaTotal(){
        return notaTotal;
    }

    public void setNotaTotal(BigDecimal notaTotal){
        this.notaTotal=notaTotal;
    }

    public int getPendentesCorrecao(){
        return pendentesCorrecao;
    }

    public void setPendentesCorrecao(int pendentesCorrecao){
        this.pendentesCorrecao=pendentesCorrecao;
    }

    public List<QuestaoResultadoDTO>getQuestoes(){
        return questoes;
    }

    public void setQuestoes(List<QuestaoResultadoDTO>questoes){
        this.questoes=questoes;
    }
}
