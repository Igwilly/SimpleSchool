package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoDTO {
    private int codigo;
    private String titulo;
    private String descricao;
    private String materia;
    private int idProfessor;
    private List<ItemAvaliacaoDTO>itens=new ArrayList<>();
    private List<Integer>turmasIds=new ArrayList<>();

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public int getIdProfessor() {
        return idProfessor;
    }

    public void setIdProfessor(int idProfessor) {
        this.idProfessor = idProfessor;
    }

    public List<ItemAvaliacaoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemAvaliacaoDTO> itens) {
        this.itens = itens;
    }

    public List<Integer> getTurmasIds() {
        return turmasIds;
    }

    public void setTurmasIds(List<Integer> turmasIds) {
        this.turmasIds = turmasIds;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }
}
