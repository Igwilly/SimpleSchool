const BASE_URL="http://localhost:8080/api";

async function request(path, options={}){
    const salvo=localStorage.getItem("usuario_sessao");
    const sessao=salvo?JSON.parse(salvo):null;
    const headers={"Content-Type":"application/json", ...(options.headers||{})};
    if(sessao?.tipo){
        headers["X-Usuario-Tipo"]=sessao.tipo;
    }

    const res=await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers
    });
    if(res.status===204){
        return null;
    }
    const text=await res.text();
    const data=text?JSON.parse(text):null;
    if(!res.ok){
        throw new Error(data?.erro||`Erro HTTP ${res.status}`);
    }
    return data;
}

export const api={
    login:(credenciais)=>request("/auth/login", {method:"POST", body:JSON.stringify(credenciais)}),
    listarTurmas:()=>request("/turmas"),
    criarTurma:(turma)=>request("/turmas", {method:"POST", body:JSON.stringify(turma)}),
    excluirTurma:(codigo)=>request(`/turmas/${codigo}`, {method:"DELETE"}),
    listarQuestoes:()=>request("/questoes"),
    criarQuestao:(questao)=>request("/questoes", {method:"POST", body:JSON.stringify(questao)}),
    excluirQuestao:(codigo)=>request(`/questoes/${codigo}`, {method:"DELETE"}),
    listarAvaliacoes:()=>request("/avaliacoes"),
    listarAvaliacoesPorTurma:(codigoTurma)=>request(`/avaliacoes/turma/${codigoTurma}`),
    criarAvaliacao:(avaliacao)=>request("/avaliacoes", {method:"POST", body:JSON.stringify(avaliacao)}),
    adicionarItemAvaliacao:(codigo, item)=>request(`/avaliacoes/${codigo}/itens`, {method:"POST", body:JSON.stringify(item)}),
    removerItemAvaliacao:(codigo, codigoQuestao)=>request(`/avaliacoes/${codigo}/itens/${codigoQuestao}`, {method:"DELETE"}),
    definirTurmasAvaliacao:(codigo, turmasIds)=>request(`/avaliacoes/${codigo}/turmas`, {method:"PUT", body:JSON.stringify(turmasIds)}),
    listarAvaliacoesAluno:(idAluno)=>request(`/realizacoes/aluno/${idAluno}`),
    carregarProvaAluno:(idAluno, codigoAvaliacao)=>request(`/realizacoes/aluno/${idAluno}/prova/${codigoAvaliacao}`),
    submeterAvaliacao:(payload)=>request("/realizacoes/submeter", {method:"POST", body:JSON.stringify(payload)}),
    buscarResultadoAluno:(idAluno, codigoAvaliacao)=>request(`/realizacoes/aluno/${idAluno}/resultado/${codigoAvaliacao}`),
    listarDissertativas:(apenasPendentes)=>request(`/realizacoes/dissertativas?apenasPendentes=${apenasPendentes}`),
    corrigirDissertativa:(payload)=>request("/realizacoes/dissertativas/corrigir", {method:"PUT", body:JSON.stringify(payload)}),
    listarNotasTotais:(codigoAvaliacao)=>request(codigoAvaliacao?`/realizacoes/notas?codigoAvaliacao=${codigoAvaliacao}`:"/realizacoes/notas"),
    listarAlunosRelatorio:()=>request("/relatorios/alunos"),
    relatorioAvaliacoes:(params)=>{
        const qs=new URLSearchParams();
        if(params.inicio){
            qs.set("inicio", params.inicio);
        }
        if(params.fim){
            qs.set("fim", params.fim);
        }
        if(params.codigoAvaliacao){
            qs.set("codigoAvaliacao", params.codigoAvaliacao);
        }
        if(params.codigoTurma){
            qs.set("codigoTurma", params.codigoTurma);
        }
        if(params.minRespondentes){
            qs.set("minRespondentes", params.minRespondentes);
        }
        return request(`/relatorios/avaliacoes?${qs.toString()}`);
    },
    relatorioRankingQuestoes:(limite, termo)=>{
        const qs=new URLSearchParams();
        qs.set("limite", limite||10);
        if(termo){
            qs.set("termo", termo);
        }
        return request(`/relatorios/questoes-ranking?${qs.toString()}`);
    },
    relatorioEvolucao:(codigoTurma, idAluno)=>{
        const qs=new URLSearchParams();
        if(codigoTurma){
            qs.set("codigoTurma", codigoTurma);
        }
        if(idAluno){
            qs.set("idAluno", idAluno);
        }
        const str=qs.toString();
        return request(str?`/relatorios/evolucao?${str}`:"/relatorios/evolucao");
    },
    relatorioEvolucaoAluno:(idAluno)=>request(`/relatorios/evolucao/${idAluno}`),
    relatorioPendencias:(turmasIds)=>{
        const qs=new URLSearchParams();
        (turmasIds||[]).forEach((id)=>qs.append("turmas", id));
        const str=qs.toString();
        return request(str?`/relatorios/pendencias?${str}`:"/relatorios/pendencias");
    }
};
