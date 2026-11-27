package com.avaliacoes.backend.model;

import java.math.BigDecimal;

public class ItemAvaliacaoDTO{
    private int codigoAvaliacao;
    private int codigoQuestao;
    private String enunciado;
    private String tipo;
    private BigDecimal valor;

    public int getCodigoAvaliacao(){
        return codigoAvaliacao;
    }

    public void setCodigoAvaliacao(int codigoAvaliacao){
        this.codigoAvaliacao=codigoAvaliacao;
    }

    public int getCodigoQuestao(){
        return codigoQuestao;
    }

    public void setCodigoQuestao(int codigoQuestao){
        this.codigoQuestao=codigoQuestao;
    }

    public String getEnunciado(){
        return enunciado;
    }

    public void setEnunciado(String enunciado){
        this.enunciado=enunciado;
    }

    public String getTipo(){
        return tipo;
    }

    public void setTipo(String tipo){
        this.tipo=tipo;
    }

    public BigDecimal getValor(){
        return valor;
    }

    public void setValor(BigDecimal valor){
        this.valor=valor;
    }
}
