package com.avaliacoes.backend.model;

import java.math.BigDecimal;

public class QuestaoResultadoDTO{
    private int codigoQuestao;
    private String enunciado;
    private String tipo;
    private String imagem;
    private BigDecimal valor=BigDecimal.ZERO;
    private BigDecimal notaObtida=BigDecimal.ZERO;
    private String letraMarcada;
    private String letraCorreta;
    private String dissertacao;
    private boolean corrigida;

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

    public String getImagem(){
        return imagem;
    }

    public void setImagem(String imagem){
        this.imagem=imagem;
    }

    public BigDecimal getValor(){
        return valor;
    }

    public void setValor(BigDecimal valor){
        this.valor=valor;
    }

    public BigDecimal getNotaObtida(){
        return notaObtida;
    }

    public void setNotaObtida(BigDecimal notaObtida){
        this.notaObtida=notaObtida;
    }

    public String getLetraMarcada(){
        return letraMarcada;
    }

    public void setLetraMarcada(String letraMarcada){
        this.letraMarcada=letraMarcada;
    }

    public String getLetraCorreta(){
        return letraCorreta;
    }

    public void setLetraCorreta(String letraCorreta){
        this.letraCorreta=letraCorreta;
    }

    public String getDissertacao(){
        return dissertacao;
    }

    public void setDissertacao(String dissertacao){
        this.dissertacao=dissertacao;
    }

    public boolean isCorrigida(){
        return corrigida;
    }

    public void setCorrigida(boolean corrigida){
        this.corrigida=corrigida;
    }
}
