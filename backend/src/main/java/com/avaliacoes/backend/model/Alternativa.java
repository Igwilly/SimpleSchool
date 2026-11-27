package com.avaliacoes.backend.model;

public class Alternativa{
    private int codigoQuestao;
    private String letra;
    private String texto;
    private boolean correta;

    public int getCodigoQuestao(){
        return codigoQuestao;
    }

    public void setCodigoQuestao(int codigoQuestao){
        this.codigoQuestao=codigoQuestao;
    }

    public String getLetra(){
        return letra;
    }

    public void setLetra(String letra){
        this.letra=letra;
    }

    public String getTexto(){
        return texto;
    }

    public void setTexto(String texto){
        this.texto=texto;
    }

    public boolean isCorreta(){
        return correta;
    }

    public void setCorreta(boolean correta){
        this.correta=correta;
    }
}
