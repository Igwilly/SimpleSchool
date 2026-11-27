package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.model.QuestaoDTO;
import com.avaliacoes.backend.service.QuestaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController{
    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService){
        this.questaoService=questaoService;
    }

    private void exigirProfessor(String tipoUsuario){
        if(!"PROFESSOR".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas professores podem gerenciar questoes.");
        }
    }

    @GetMapping
    public List<QuestaoDTO>listarTodas(@RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario){
        if("ALUNO".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Alunos nao tem acesso ao banco de questoes.");
        }
        return questaoService.listarTodas();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<QuestaoDTO>buscarPorCodigo(@PathVariable int codigo){
        return questaoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<QuestaoDTO>inserir(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestBody QuestaoDTO dto){
        exigirProfessor(tipoUsuario);
        return ResponseEntity.ok(questaoService.inserir(dto));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void>excluir(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo){
        exigirProfessor(tipoUsuario);
        int linhas=questaoService.excluir(codigo);
        return linhas>0?ResponseEntity.noContent().build():ResponseEntity.notFound().build();
    }
}
