package com.avaliacoes.backend.model;

import java.math.BigDecimal;

public class NotaAlunoAvaliacaoDTO{
    private int idAluno;
    private String nomeAluno;
    private String turmaNome;
    private int codigoAvaliacao;
    private String tituloAvaliacao;
    private String data;
    private BigDecimal valorTotal=BigDecimal.ZERO;
    private BigDecimal notaTotal=BigDecimal.ZERO;
    private int pendentesCorrecao;

    public int getIdAluno(){
        return idAluno;
    }

    public void setIdAluno(int idAluno){
        this.idAluno=idAluno;
    }

    public String getNomeAluno(){
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno){
        this.nomeAluno=nomeAluno;
    }

    public String getTurmaNome(){
        return turmaNome;
    }

    public void setTurmaNome(String turmaNome){
        this.turmaNome=turmaNome;
    }

    public int getCodigoAvaliacao(){
        return codigoAvaliacao;
    }

    public void setCodigoAvaliacao(int codigoAvaliacao){
        this.codigoAvaliacao=codigoAvaliacao;
    }

    public String getTituloAvaliacao(){
        return tituloAvaliacao;
    }

    public void setTituloAvaliacao(String tituloAvaliacao){
        this.tituloAvaliacao=tituloAvaliacao;
    }

    public String getData(){
        return data;
    }

    public void setData(String data){
        this.data=data;
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
}
