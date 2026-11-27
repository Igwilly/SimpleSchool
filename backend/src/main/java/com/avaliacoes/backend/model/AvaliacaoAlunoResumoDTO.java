package com.avaliacoes.backend.model;

import java.math.BigDecimal;

public class AvaliacaoAlunoResumoDTO{
    private int codigoAvaliacao;
    private String titulo;
    private String materia;
    private String descricao;
    private String dataAvaliacao;
    private String horaInicio;
    private String horaFim;
    private int qtdQuestoes;
    private BigDecimal valorTotal=BigDecimal.ZERO;
    private boolean respondida;
    private String dataRealizacao;
    private BigDecimal notaObtida=BigDecimal.ZERO;
    private int pendentesCorrecao;

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

    public String getDescricao(){
        return descricao;
    }

    public void setDescricao(String descricao){
        this.descricao=descricao;
    }

    public String getDataAvaliacao(){
        return dataAvaliacao;
    }

    public void setDataAvaliacao(String dataAvaliacao){
        this.dataAvaliacao=dataAvaliacao;
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

    public int getQtdQuestoes(){
        return qtdQuestoes;
    }

    public void setQtdQuestoes(int qtdQuestoes){
        this.qtdQuestoes=qtdQuestoes;
    }

    public BigDecimal getValorTotal(){
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal){
        this.valorTotal=valorTotal;
    }

    public boolean isRespondida(){
        return respondida;
    }

    public void setRespondida(boolean respondida){
        this.respondida=respondida;
    }

    public String getDataRealizacao(){
        return dataRealizacao;
    }

    public void setDataRealizacao(String dataRealizacao){
        this.dataRealizacao=dataRealizacao;
    }

    public BigDecimal getNotaObtida(){
        return notaObtida;
    }

    public void setNotaObtida(BigDecimal notaObtida){
        this.notaObtida=notaObtida;
    }

    public int getPendentesCorrecao(){
        return pendentesCorrecao;
    }

    public void setPendentesCorrecao(int pendentesCorrecao){
        this.pendentesCorrecao=pendentesCorrecao;
    }
}
