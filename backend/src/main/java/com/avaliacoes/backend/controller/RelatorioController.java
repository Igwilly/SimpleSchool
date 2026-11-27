package com.avaliacoes.backend.controller;

import com.avaliacoes.backend.service.RelatorioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController{
    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService){
        this.relatorioService=relatorioService;
    }

    private void exigirProfessor(String tipoUsuario){
        if(!"PROFESSOR".equalsIgnoreCase(tipoUsuario)){
            throw new SecurityException("Apenas professores podem acessar os relatorios gerais.");
        }
    }

    @GetMapping("/alunos")
    public List<Map<String, Object>>listarAlunos(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario){
        exigirProfessor(tipoUsuario);
        return relatorioService.listarAlunos();
    }

    @GetMapping("/avaliacoes")
    public List<Map<String, Object>>desempenhoAvaliacoes(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(required=false) String inicio,
            @RequestParam(required=false) String fim,
            @RequestParam(required=false) Integer codigoAvaliacao,
            @RequestParam(required=false) Integer codigoTurma,
            @RequestParam(required=false) Integer minRespondentes){
        exigirProfessor(tipoUsuario);
        return relatorioService.desempenhoAvaliacoes(inicio, fim, codigoAvaliacao, codigoTurma, minRespondentes);
    }

    @GetMapping("/questoes-ranking")
    public List<Map<String, Object>>rankingQuestoes(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(defaultValue="10") Integer limite,
            @RequestParam(required=false) String termo){
        exigirProfessor(tipoUsuario);
        return relatorioService.rankingQuestoes(limite, termo);
    }

    @GetMapping("/evolucao")
    public List<Map<String, Object>>evolucaoGeralOuTurma(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(required=false) Integer codigoTurma,
            @RequestParam(required=false) Integer idAluno){
        exigirProfessor(tipoUsuario);
        return relatorioService.evolucao(codigoTurma, idAluno);
    }

    @GetMapping("/evolucao/{idAluno}")
    public List<Map<String, Object>>evolucaoAluno(@PathVariable int idAluno){
        return relatorioService.evolucaoAluno(idAluno);
    }

    @GetMapping("/pendencias")
    public Map<String, Object>pendencias(
            @RequestHeader(value="X-Usuario-Tipo", required=false) String tipoUsuario,
            @RequestParam(required=false) List<Integer>turmas){
        exigirProfessor(tipoUsuario);
        return relatorioService.pendenciasEQuestoesSemUso(turmas);
    }
}
