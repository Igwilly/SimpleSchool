import React, {useEffect, useState} from "react";
import {api} from "../api.js";

export default function CorrecoesPage({usuario, notificar}){
    const [dissertativas, setDissertativas]=useState([]);
    const [notasDigitadas, setNotasDigitadas]=useState({});
    const [filtroSituacao, setFiltroSituacao]=useState("PENDENTES");
    const [filtroAvaliacaoDiss, setFiltroAvaliacaoDiss]=useState("");
    const [avaliacoes, setAvaliacoes]=useState([]);
    const [turmas, setTurmas]=useState([]);
    const [filtroAvaliacaoNotas, setFiltroAvaliacaoNotas]=useState("");
    const [filtroTurmaNotas, setFiltroTurmaNotas]=useState("");
    const [notasTotais, setNotasTotais]=useState([]);
    const [sucesso, setSucesso]=useState("");

    async function carregarDissertativas(situacao){
        try{
            const apenasPend=situacao==="PENDENTES";
            const lista=await api.listarDissertativas(apenasPend);
            setDissertativas(lista);
            const mapa={};
            lista.forEach((item)=>{
                const chave=`${item.idAluno}_${item.codigoAvaliacao}_${item.codigoQuestao}`;
                mapa[chave]=Number(item.nota).toFixed(2);
            });
            setNotasDigitadas(mapa);
        }catch(err){
            notificar("Erro ao carregar respostas", err.message);
        }
    }

    async function carregarNotasTotais(codAv){
        try{
            setNotasTotais(await api.listarNotasTotais(codAv||null));
        }catch(err){
            notificar("Erro ao carregar notas", err.message);
        }
    }

    useEffect(()=>{
        api.listarAvaliacoes().then(setAvaliacoes).catch((err)=>notificar("Erro", err.message));
        api.listarTurmas().then(setTurmas).catch((err)=>notificar("Erro", err.message));
    }, []);

    useEffect(()=>{
        carregarDissertativas(filtroSituacao);
    }, [filtroSituacao]);

    useEffect(()=>{
        carregarNotasTotais(filtroAvaliacaoNotas);
    }, [filtroAvaliacaoNotas]);

    function alterarNota(chave, valorTexto, valorMax){
        if(valorTexto===""){
            setNotasDigitadas((prev)=>({...prev, [chave]:""}));
            return;
        }
        const n=Number(valorTexto);
        const max=Number(valorMax);
        if(isNaN(n)){
            return;
        }
        if(n>max){
            const teto=max.toFixed(2);
            setNotasDigitadas((prev)=>({...prev, [chave]:teto}));
            notificar("Nota limitada", `A nota maxima desta questao e ${teto}. Ajustada automaticamente.`);
            return;
        }
        if(n<0){
            setNotasDigitadas((prev)=>({...prev, [chave]:"0.00"}));
            notificar("Nota limitada", "A nota minima e 0.00. Ajustada automaticamente.");
            return;
        }
        setNotasDigitadas((prev)=>({...prev, [chave]:valorTexto}));
    }

    async function salvarCorrecao(item){
        const chave=`${item.idAluno}_${item.codigoAvaliacao}_${item.codigoQuestao}`;
        setSucesso("");
        try{
            const bruto=notasDigitadas[chave];
            const notaFinal=(bruto===""||isNaN(Number(bruto)))?0:Number(bruto);
            await api.corrigirDissertativa({
                idAluno:item.idAluno,
                codigoAvaliacao:item.codigoAvaliacao,
                codigoQuestao:item.codigoQuestao,
                nota:notaFinal,
                idProfessor:usuario.id
            });
            setSucesso(`Nota salva para ${item.nomeAluno} na questao #${item.codigoQuestao}.`);
            await carregarDissertativas(filtroSituacao);
            await carregarNotasTotais(filtroAvaliacaoNotas);
        }catch(err){
            notificar("Erro ao salvar nota", err.message);
        }
    }

    const dissertativasFiltradas=dissertativas.filter((item)=>{
        if(filtroSituacao==="CORRIGIDAS"&&!item.dataCorrecao){
            return false;
        }
        if(filtroAvaliacaoDiss&&item.codigoAvaliacao!==Number(filtroAvaliacaoDiss)){
            return false;
        }
        return true;
    });

    const notasFiltradas=notasTotais.filter((n)=>
        !filtroTurmaNotas||n.turmaNome===filtroTurmaNotas
    );

    return (
        <div>
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Correcao de questoes dissertativas ({dissertativasFiltradas.length})</legend>
                <p>
                    <label>
                        Situacao:{" "}
                        <select value={filtroSituacao} onChange={(e)=>setFiltroSituacao(e.target.value)}>
                            <option value="PENDENTES">Apenas pendentes</option>
                            <option value="CORRIGIDAS">Apenas corrigidas</option>
                            <option value="TODAS">Todas</option>
                        </select>
                    </label>{" | "}
                    <label>
                        Avaliacao:{" "}
                        <select value={filtroAvaliacaoDiss} onChange={(e)=>setFiltroAvaliacaoDiss(e.target.value)}>
                            <option value="">Todas as avaliacoes</option>
                            {avaliacoes.map((av)=>(
                                <option key={av.codigo} value={av.codigo}>
                                    #{av.codigo} - {av.titulo}
                                </option>
                            ))}
                        </select>
                    </label>
                </p>

                {dissertativasFiltradas.length===0?(
                    <p>Nenhuma resposta dissertativa encontrada para este filtro.</p>
                ):(
                    <table border="1" cellPadding="5">
                        <thead>
                            <tr>
                                <th>Avaliacao</th>
                                <th>Aluno</th>
                                <th>Questao e resposta</th>
                                <th>Situacao</th>
                                <th>Nota (min e max)</th>
                            </tr>
                        </thead>
                        <tbody>
                            {dissertativasFiltradas.map((item)=>{
                                const chave=`${item.idAluno}_${item.codigoAvaliacao}_${item.codigoQuestao}`;
                                const teto=Number(item.valorMaximo).toFixed(2);
                                return (
                                    <tr key={chave}>
                                        <td>#{item.codigoAvaliacao} - {item.tituloAvaliacao}</td>
                                        <td>{item.nomeAluno}</td>
                                        <td>
                                            <div><strong>Q#{item.codigoQuestao}:</strong> {item.enunciado}</div>
                                            <div><em>Esperada:</em> {item.respostaEsperada}</div>
                                            <div><strong>Resposta do aluno:</strong> {item.dissertacao}</div>
                                        </td>
                                        <td>
                                            {item.dataCorrecao?`Corrigida em ${item.dataCorrecao}`:"Pendente"}
                                        </td>
                                        <td>
                                            <div><small>min 0.00 | max {teto}</small></div>
                                            <input
                                                type="number"
                                                step="0.25"
                                                min="0"
                                                max={item.valorMaximo}
                                                placeholder={`0.00 a ${teto}`}
                                                value={notasDigitadas[chave]??""}
                                                onChange={(e)=>alterarNota(chave, e.target.value, item.valorMaximo)}
                                            />{" "}
                                            <button type="button" onClick={()=>salvarCorrecao(item)}>
                                                Salvar nota
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                )}
            </fieldset>

            <br />

            <fieldset>
                <legend>Notas totais dos alunos ({notasFiltradas.length})</legend>
                <p>
                    <label>
                        Avaliacao:{" "}
                        <select value={filtroAvaliacaoNotas} onChange={(e)=>setFiltroAvaliacaoNotas(e.target.value)}>
                            <option value="">Todas as avaliacoes</option>
                            {avaliacoes.map((av)=>(
                                <option key={av.codigo} value={av.codigo}>
                                    #{av.codigo} - {av.titulo}
                                </option>
                            ))}
                        </select>
                    </label>{" | "}
                    <label>
                        Turma:{" "}
                        <select value={filtroTurmaNotas} onChange={(e)=>setFiltroTurmaNotas(e.target.value)}>
                            <option value="">Todas as turmas</option>
                            {turmas.map((t)=>(
                                <option key={t.codigo} value={t.nome}>{t.nome}</option>
                            ))}
                        </select>
                    </label>
                </p>

                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Avaliacao</th>
                            <th>Turma</th>
                            <th>Aluno</th>
                            <th>Data</th>
                            <th>Situacao</th>
                            <th>Nota total</th>
                        </tr>
                    </thead>
                    <tbody>
                        {notasFiltradas.map((n)=>(
                            <tr key={`${n.idAluno}_${n.codigoAvaliacao}_${n.data}`}>
                                <td>#{n.codigoAvaliacao} - {n.tituloAvaliacao}</td>
                                <td>{n.turmaNome}</td>
                                <td>{n.nomeAluno}</td>
                                <td>{n.data}</td>
                                <td>
                                    {n.pendentesCorrecao>0
                                        ?`${n.pendentesCorrecao} dissertativa(s) pendente(s)`
                                        :"Corrigida"}
                                </td>
                                <td>
                                    <strong>{Number(n.notaTotal).toFixed(2)}</strong> / {Number(n.valorTotal).toFixed(2)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </fieldset>
        </div>
    );
}
