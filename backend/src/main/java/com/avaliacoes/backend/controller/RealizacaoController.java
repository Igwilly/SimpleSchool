package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.model.*;
import com.avaliacoes.backend.service.RealizacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realizacoes")
public class RealizacaoController{
    private final RealizacaoService realizacaoService;

    public RealizacaoController(RealizacaoService realizacaoService){
        this.realizacaoService=realizacaoService;
    }

    private void exigirProfessor(String tipoUsuario){
        if(!"PROFESSOR".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas professores podem acessar esta operacao.");
        }
    }

    private void exigirAluno(String tipoUsuario){
        if(!"ALUNO".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas alunos podem realizar avaliacoes.");
        }
    }

    @GetMapping("/aluno/{idAluno}")
    public List<AvaliacaoAlunoResumoDTO>listarDoAluno(@PathVariable int idAluno){
        return realizacaoService.listarAvaliacoesDoAluno(idAluno);
    }

    @GetMapping("/aluno/{idAluno}/prova/{codigoAvaliacao}")
    public ResponseEntity<Map<String, Object>>carregarProva(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @PathVariable int idAluno,
            @PathVariable int codigoAvaliacao){
        exigirAluno(tipoUsuario);
        return realizacaoService.carregarProvaParaResponder(idAluno, codigoAvaliacao)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/submeter")
    public ResponseEntity<ResultadoAvaliacaoDTO>submeter(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestBody SubmissaoAvaliacaoDTO dto){
        exigirAluno(tipoUsuario);
        return ResponseEntity.ok(realizacaoService.submeter(dto));
    }

    @GetMapping("/aluno/{idAluno}/resultado/{codigoAvaliacao}")
    public ResponseEntity<ResultadoAvaliacaoDTO>buscarResultado(
            @PathVariable int idAluno,
            @PathVariable int codigoAvaliacao){
        return realizacaoService.buscarResultadoAluno(idAluno, codigoAvaliacao)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dissertativas")
    public List<CorrecaoDissertativaDTO>listarDissertativas(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(defaultValue="false") boolean apenasPendentes){
        exigirProfessor(tipoUsuario);
        return realizacaoService.listarRespostasDissertativas(apenasPendentes);
    }

    @PutMapping("/dissertativas/corrigir")
    public ResponseEntity<Void>corrigirDissertativa(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestBody CorrecaoDissertativaDTO dto){
        exigirProfessor(tipoUsuario);
        realizacaoService.corrigirDissertativa(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notas")
    public List<NotaAlunoAvaliacaoDTO>listarNotas(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(required=false) Integer codigoAvaliacao){
        exigirProfessor(tipoUsuario);
        return realizacaoService.listarNotasTotais(codigoAvaliacao);
    }
}
