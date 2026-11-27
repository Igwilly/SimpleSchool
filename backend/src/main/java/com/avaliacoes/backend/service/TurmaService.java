package com.avaliacoes.backend.service;

import com.avaliacoes.backend.dao.TurmaDAO;
import com.avaliacoes.backend.model.Turma;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TurmaService{
    private final TurmaDAO turmaDAO;

    public TurmaService(TurmaDAO turmaDAO){
        this.turmaDAO=turmaDAO;
    }

    public void inserir(Turma turma){
        turmaDAO.inserir(turma);
    }

    public List<Turma>listarTodas(){
        return turmaDAO.listarTodas();
    }

    public Optional<Turma>buscarPorCodigo(int codigo){
        return turmaDAO.buscarPorCodigo(codigo);
    }

    public int atualizar(Turma turma){
        return turmaDAO.atualizar(turma);
    }

    public int excluir(int codigo){
        return turmaDAO.excluir(codigo);
    }
}
