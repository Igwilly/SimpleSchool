package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.model.Turma;
import com.avaliacoes.backend.service.TurmaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController{
    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService){
        this.turmaService=turmaService;
    }

    private void exigirProfessor(String tipoUsuario){
        if(!"PROFESSOR".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas professores podem gerenciar turmas.");
        }
    }

    @GetMapping
    public List<Turma>listarTodas(){
        return turmaService.listarTodas();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Turma>buscarPorCodigo(@PathVariable int codigo){
        return turmaService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Turma>inserir(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestBody Turma turma){
        exigirProfessor(tipoUsuario);
        turmaService.inserir(turma);
        return ResponseEntity.ok(turma);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<Void>atualizar(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo,
            @RequestBody Turma turma){
        exigirProfessor(tipoUsuario);
        turma.setCodigo(codigo);
        int linhas=turmaService.atualizar(turma);
        return linhas>0?ResponseEntity.ok().build():ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void>excluir(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo){
        exigirProfessor(tipoUsuario);
        int linhas=turmaService.excluir(codigo);
        return linhas>0?ResponseEntity.noContent().build():ResponseEntity.notFound().build();
    }
}
