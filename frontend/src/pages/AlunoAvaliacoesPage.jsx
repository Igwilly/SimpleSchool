import React, {useEffect, useState} from "react";
import {api} from "../api.js";

function horarioAtual(){
    const d=new Date();
    const hh=String(d.getHours()).padStart(2, "0");
    const mm=String(d.getMinutes()).padStart(2, "0");
    const ss=String(d.getSeconds()).padStart(2, "0");
    return `${hh}:${mm}:${ss}`;
}

export default function AlunoAvaliacoesPage({usuario, notificar}){
    const [avaliacoes, setAvaliacoes]=useState([]);
    const [modo, setModo]=useState("lista");
    const [provaAtual, setProvaAtual]=useState(null);
    const [horaInicio, setHoraInicio]=useState("");
    const [respostas, setRespostas]=useState({});
    const [resultado, setResultado]=useState(null);
    const [enviando, setEnviando]=useState(false);

    async function carregarLista(){
        try{
            setAvaliacoes(await api.listarAvaliacoesAluno(usuario.id));
        }catch(err){
            notificar("Erro ao carregar avaliacoes", err.message);
        }
    }

    useEffect(()=>{
        if(usuario?.id){
            carregarLista();
        }
    }, [usuario]);

    async function iniciarProva(codigoAvaliacao){
        try{
            const prova=await api.carregarProvaAluno(usuario.id, codigoAvaliacao);
            setProvaAtual(prova);
            setRespostas({});
            setHoraInicio(horarioAtual());
            setModo("responder");
        }catch(err){
            notificar("Erro ao iniciar avaliacao", err.message);
        }
    }

    async function abrirResultado(codigoAvaliacao){
        try{
            const res=await api.buscarResultadoAluno(usuario.id, codigoAvaliacao);
            setResultado(res);
            setModo("resultado");
        }catch(err){
            notificar("Erro ao abrir resultado", err.message);
        }
    }

    function marcarAlternativa(codigoQuestao, letra){
        setRespostas((prev)=>({...prev, [codigoQuestao]:{...prev[codigoQuestao], letraAlternativa:letra}}));
    }

    function digitarDissertacao(codigoQuestao, texto){
        setRespostas((prev)=>({...prev, [codigoQuestao]:{...prev[codigoQuestao], dissertacao:texto}}));
    }

    async function enviarProva(e){
        e.preventDefault();
        setEnviando(true);
        try{
            const listaRespostas=provaAtual.questoes.map((q)=>({
                codigoQuestao:q.codigoQuestao,
                letraAlternativa:respostas[q.codigoQuestao]?.letraAlternativa||null,
                dissertacao:respostas[q.codigoQuestao]?.dissertacao||null
            }));
            const res=await api.submeterAvaliacao({
                idAluno:usuario.id,
                codigoAvaliacao:provaAtual.codigo,
                horaInicio,
                respostas:listaRespostas
            });
            setResultado(res);
            setProvaAtual(null);
            setModo("resultado");
            await carregarLista();
        }catch(err){
            notificar("Erro ao enviar respostas", err.message);
        }finally{
            setEnviando(false);
        }
    }

    function voltarLista(){
        setModo("lista");
        setProvaAtual(null);
        setResultado(null);
    }

    if(modo==="responder"&&provaAtual){
        return (
            <fieldset>
                <legend>Responder avaliacao: {provaAtual.titulo}</legend>
                <p>
                    <strong>Materia:</strong> {provaAtual.materia} |{" "}
                    <strong>Valor total:</strong> {Number(provaAtual.valorTotal).toFixed(2)} pts |{" "}
                    <strong>Inicio:</strong> {horaInicio}
                </p>
                {provaAtual.descricao&&<p>{provaAtual.descricao}</p>}

                <form onSubmit={enviarProva}>
                    {provaAtual.questoes.map((q, idx)=>(
                        <div key={q.codigoQuestao}>
                            <fieldset>
                                <legend>
                                    Questao {idx+1} (#{q.codigoQuestao}) - {Number(q.valor).toFixed(2)} pts
                                </legend>
                                <p>{q.enunciado}</p>
                                {q.imagem&&(
                                    <p>
                                        <img src={q.imagem} alt={`Imagem Q#${q.codigoQuestao}`} width={280} />
                                    </p>
                                )}
                                {q.tipo==="OBJETIVA"?(
                                    <div>
                                        {q.alternativas.map((alt)=>(
                                            <p key={alt.letra}>
                                                <label>
                                                    <input
                                                        type="radio"
                                                        name={`questao_${q.codigoQuestao}`}
                                                        checked={respostas[q.codigoQuestao]?.letraAlternativa===alt.letra}
                                                        onChange={()=>marcarAlternativa(q.codigoQuestao, alt.letra)}
                                                        required
                                                    />{" "}
                                                    {alt.letra}) {alt.texto}
                                                </label>
                                            </p>
                                        ))}
                                    </div>
                                ):(
                                    <p>
                                        <textarea
                                            rows={4}
                                            cols={75}
                                            placeholder="Digite sua resposta dissertativa (obrigatorio)"
                                            value={respostas[q.codigoQuestao]?.dissertacao||""}
                                            onChange={(e)=>digitarDissertacao(q.codigoQuestao, e.target.value)}
                                            required
                                        />
                                    </p>
                                )}
                            </fieldset>
                            <br />
                        </div>
                    ))}

                    <p>
                        <button type="submit" disabled={enviando}>
                            {enviando?"Enviando...":"Enviar respostas"}
                        </button>{" "}
                        <button type="button" onClick={voltarLista}>Cancelar</button>
                    </p>
                </form>
            </fieldset>
        );
    }

    if(modo==="resultado"&&resultado){
        return (
            <fieldset>
                <legend>Resultado da avaliacao: {resultado.titulo}</legend>
                <p>
                    <strong>Materia:</strong> {resultado.materia} |{" "}
                    <strong>Data:</strong> {resultado.data} ({resultado.horaInicio} ate {resultado.horaFim})
                </p>
                <p>
                    <strong>Nota total:</strong> {Number(resultado.notaTotal).toFixed(2)} / {Number(resultado.valorTotal).toFixed(2)} pts
                    {resultado.pendentesCorrecao>0
                        ?` (${resultado.pendentesCorrecao} dissertativa(s) aguardando correcao do professor)`
                        :""}
                </p>

                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Tipo</th>
                            <th>Enunciado e resposta</th>
                            <th>Valor</th>
                            <th>Nota obtida</th>
                        </tr>
                    </thead>
                    <tbody>
                        {resultado.questoes.map((q)=>(
                            <tr key={q.codigoQuestao}>
                                <td>{q.codigoQuestao}</td>
                                <td>{q.tipo}</td>
                                <td>
                                    <div>{q.enunciado}</div>
                                    {q.imagem&&(
                                        <div>
                                            <img src={q.imagem} alt={`Imagem Q#${q.codigoQuestao}`} width={200} />
                                        </div>
                                    )}
                                    {q.tipo==="OBJETIVA"?(
                                        <div>
                                            Marcada: <strong>{q.letraMarcada}</strong> | Correta: <strong>{q.letraCorreta}</strong>
                                        </div>
                                    ):(
                                        <div><em>Sua resposta:</em> {q.dissertacao}</div>
                                    )}
                                </td>
                                <td>{Number(q.valor).toFixed(2)}</td>
                                <td>
                                    {q.corrigida
                                        ?Number(q.notaObtida).toFixed(2)
                                        :"Aguardando correcao"}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                <p>
                    <button type="button" onClick={voltarLista}>Voltar</button>
                </p>
            </fieldset>
        );
    }

    return (
        <fieldset>
            <legend>Minhas avaliacoes</legend>
            {avaliacoes.length===0?(
                <p>Nenhuma avaliacao destinada para sua turma no momento.</p>
            ):(
                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Titulo</th>
                            <th>Materia</th>
                            <th>Data e horario</th>
                            <th>Questoes</th>
                            <th>Valor total</th>
                            <th>Situacao</th>
                            <th>Nota total</th>
                            <th>Acao</th>
                        </tr>
                    </thead>
                    <tbody>
                        {avaliacoes.map((av)=>(
                            <tr key={av.codigoAvaliacao}>
                                <td>{av.codigoAvaliacao}</td>
                                <td>{av.titulo}</td>
                                <td>{av.materia}</td>
                                <td>
                                    {av.dataAvaliacao?`${av.dataAvaliacao} (${av.horaInicio} as ${av.horaFim})`:"-"}
                                </td>
                                <td>{av.qtdQuestoes}</td>
                                <td>{Number(av.valorTotal).toFixed(2)}</td>
                                <td>
                                    {!av.respondida
                                        ?"Pendente"
                                        :av.pendentesCorrecao>0
                                            ?"Aguardando correcao"
                                            :`Concluida (${av.dataRealizacao})`}
                                </td>
                                <td>
                                    {av.respondida
                                        ?`${Number(av.notaObtida).toFixed(2)} / ${Number(av.valorTotal).toFixed(2)}`
                                        :"-"}
                                </td>
                                <td>
                                    {av.respondida?(
                                        <button type="button" onClick={()=>abrirResultado(av.codigoAvaliacao)}>
                                            Ver resultado
                                        </button>
                                    ):(
                                        <button type="button" onClick={()=>iniciarProva(av.codigoAvaliacao)}>
                                            Responder
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </fieldset>
    );
}
