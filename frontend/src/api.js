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
    definirTurmasAvaliacao:(codigo, turmasIds)=>request(`/avaliacoes/${codigo}/turmas`, {method:"PUT", body:JSON.stringify(turmasIds)})
};
