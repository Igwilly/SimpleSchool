package model;

public class UsuarioSessaoDTO {
    private int id;
    private String nome;
    private String email;
    private String tipo;
    private Integer turmaCoddigo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getTurmaCoddigo() {
        return turmaCoddigo;
    }

    public void setTurmaCoddigo(Integer turmaCoddigo) {
        this.turmaCoddigo = turmaCoddigo;
    }
}
