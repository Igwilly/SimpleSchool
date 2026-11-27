package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.model.AvaliacaoDTO;
import com.avaliacoes.backend.model.ItemAvaliacaoDTO;
import com.avaliacoes.backend.service.AvaliacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController{
    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService){
        this.avaliacaoService=avaliacaoService;
    }

    private void exigirProfessor(String tipoUsuario){
        if(!"PROFESSOR".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas professores podem gerenciar avaliacoes.");
        }
    }

    @GetMapping
    public List<AvaliacaoDTO>listarTodas(){
        return avaliacaoService.listarTodas();
    }

    @GetMapping("/turma/{codigoTurma}")
    public List<AvaliacaoDTO>listarPorTurma(@PathVariable int codigoTurma){
        return avaliacaoService.listarPorTurma(codigoTurma);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<AvaliacaoDTO>buscarPorCodigo(@PathVariable int codigo){
        return avaliacaoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AvaliacaoDTO>inserir(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestBody AvaliacaoDTO dto){
        exigirProfessor(tipoUsuario);
        return ResponseEntity.ok(avaliacaoService.inserir(dto));
    }

    @PostMapping("/{codigo}/itens")
    public ResponseEntity<AvaliacaoDTO>adicionarItem(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo,
            @RequestBody ItemAvaliacaoDTO item){
        exigirProfessor(tipoUsuario);
        avaliacaoService.adicionarItem(codigo, item);
        return avaliacaoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{codigo}/itens/{codigoQuestao}")
    public ResponseEntity<Void>removerItem(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo,
            @PathVariable int codigoQuestao){
        exigirProfessor(tipoUsuario);
        int linhas=avaliacaoService.removerItem(codigo, codigoQuestao);
        return linhas>0?ResponseEntity.noContent().build():ResponseEntity.notFound().build();
    }

    @PutMapping("/{codigo}/turmas")
    public ResponseEntity<AvaliacaoDTO>definirTurmas(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int codigo,
            @RequestBody List<Integer>turmasIds){
        exigirProfessor(tipoUsuario);
        avaliacaoService.definirTurmas(codigo, turmasIds);
        return avaliacaoService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
