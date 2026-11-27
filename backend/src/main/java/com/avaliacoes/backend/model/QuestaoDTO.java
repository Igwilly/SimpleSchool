package com.avaliacoes.backend.model;

import java.util.ArrayList;
import java.util.List;

public class QuestaoDTO{
    private int codigo;
    private String enunciado;
    private String tipo;
    private String imagem;
    private String respostaEsperada;
    private List<Alternativa>alternativas=new ArrayList<>();

    public int getCodigo(){
        return codigo;
    }

    public void setCodigo(int codigo){
        this.codigo=codigo;
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

    public String getRespostaEsperada(){
        return respostaEsperada;
    }

    public void setRespostaEsperada(String respostaEsperada){
        this.respostaEsperada=respostaEsperada;
    }

    public List<Alternativa>getAlternativas(){
        return alternativas;
    }

    public void setAlternativas(List<Alternativa>alternativas){
        this.alternativas=alternativas;
    }
}
