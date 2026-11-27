package com.avaliacoes.backend.model;

public class RespostaSubmissaoItemDTO{
    private int codigoQuestao;
    private String letraAlternativa;
    private String dissertacao;

    public int getCodigoQuestao(){
        return codigoQuestao;
    }

    public void setCodigoQuestao(int codigoQuestao){
        this.codigoQuestao=codigoQuestao;
    }

    public String getLetraAlternativa(){
        return letraAlternativa;
    }

    public void setLetraAlternativa(String letraAlternativa){
        this.letraAlternativa=letraAlternativa;
    }

    public String getDissertacao(){
        return dissertacao;
    }

    public void setDissertacao(String dissertacao){
        this.dissertacao=dissertacao;
    }
}
