package com.avaliacoes.backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDTO{
    private int codigo;
    private String titulo;
    private String descricao;
    private String materia;
    private String data;
    private String horaInicio;
    private String horaFim;
    private int idProfessor;
    private String nomeProfessor;
    private BigDecimal valorTotal=BigDecimal.ZERO;
    private List<ItemAvaliacaoDTO>itens=new ArrayList<>();
    private List<Integer>turmasIds=new ArrayList<>();
    private List<String>turmasNomes=new ArrayList<>();

    public int getCodigo(){
        return codigo;
    }

    public void setCodigo(int codigo){
        this.codigo=codigo;
    }

    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String titulo){
        this.titulo=titulo;
    }

    public String getDescricao(){
        return descricao;
    }

    public void setDescricao(String descricao){
        this.descricao=descricao;
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

    public int getIdProfessor(){
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor){
        this.idProfessor=idProfessor;
    }

    public String getNomeProfessor(){
        return nomeProfessor;
    }

    public void setNomeProfessor(String nomeProfessor){
        this.nomeProfessor=nomeProfessor;
    }

    public BigDecimal getValorTotal(){
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal){
        this.valorTotal=valorTotal;
    }

    public List<ItemAvaliacaoDTO>getItens(){
        return itens;
    }

    public void setItens(List<ItemAvaliacaoDTO>itens){
        this.itens=itens;
    }

    public List<Integer>getTurmasIds(){
        return turmasIds;
    }

    public void setTurmasIds(List<Integer>turmasIds){
        this.turmasIds=turmasIds;
    }

    public List<String>getTurmasNomes(){
        return turmasNomes;
    }

    public void setTurmasNomes(List<String>turmasNomes){
        this.turmasNomes=turmasNomes;
    }
}
