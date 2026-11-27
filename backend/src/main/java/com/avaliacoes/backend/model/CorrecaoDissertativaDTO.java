package com.avaliacoes.backend.model;

import java.math.BigDecimal;

public class CorrecaoDissertativaDTO{
    private int idAluno;
    private String nomeAluno;
    private int codigoAvaliacao;
    private String tituloAvaliacao;
    private int codigoQuestao;
    private String enunciado;
    private String respostaEsperada;
    private String dissertacao;
    private BigDecimal valorMaximo=BigDecimal.ZERO;
    private BigDecimal nota=BigDecimal.ZERO;
    private String dataRealizacao;
    private String dataCorrecao;
    private int idProfessor;

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

    public String getRespostaEsperada(){
        return respostaEsperada;
    }

    public void setRespostaEsperada(String respostaEsperada){
        this.respostaEsperada=respostaEsperada;
    }

    public String getDissertacao(){
        return dissertacao;
    }

    public void setDissertacao(String dissertacao){
        this.dissertacao=dissertacao;
    }

    public BigDecimal getValorMaximo(){
        return valorMaximo;
    }

    public void setValorMaximo(BigDecimal valorMaximo){
        this.valorMaximo=valorMaximo;
    }

    public BigDecimal getNota(){
        return nota;
    }

    public void setNota(BigDecimal nota){
        this.nota=nota;
    }

    public String getDataRealizacao(){
        return dataRealizacao;
    }

    public void setDataRealizacao(String dataRealizacao){
        this.dataRealizacao=dataRealizacao;
    }

    public String getDataCorrecao(){
        return dataCorrecao;
    }

    public void setDataCorrecao(String dataCorrecao){
        this.dataCorrecao=dataCorrecao;
    }

    public int getIdProfessor(){
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor){
        this.idProfessor=idProfessor;
    }
}
