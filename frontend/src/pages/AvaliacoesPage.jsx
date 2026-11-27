import React, {useEffect, useState} from "react";
import {api} from "../api.js";

export default function AvaliacoesPage({usuario}){
    const [avaliacoes, setAvaliacoes]=useState([]);
    const [questoes, setQuestoes]=useState([]);
    const [turmas, setTurmas]=useState([]);
    const [titulo, setTitulo]=useState("");
    const [materia, setMateria]=useState("");
    const [descricao, setDescricao]=useState("");
    const [selecionadas, setSelecionadas]=useState({});
    const [turmasMarcadas, setTurmasMarcadas]=useState([]);
    const [filtroQuestao, setFiltroQuestao]=useState("");
    const [expandida, setExpandida]=useState(null);
    const [novaQuestaoExtra, setNovaQuestaoExtra]=useState("");
    const [novoValorExtra, setNovoValorExtra]=useState("1.00");
    const [erro, setErro]=useState("");
    const [sucesso, setSucesso]=useState("");

    async function carregarTudo(){
        try{
            const [avs, qs, ts]=await Promise.all([
                api.listarAvaliacoes(),
                api.listarQuestoes(),
                api.listarTurmas()
            ]);
            setAvaliacoes(avs);
            setQuestoes(qs);
            setTurmas(ts);
        }catch(err){
            setErro(err.message);
        }
    }

    useEffect(()=>{
        carregarTudo();
    }, []);

    function alternarQuestao(codigo){
        setSelecionadas((prev)=>{
            const copia={...prev};
            if(copia[codigo]!==undefined){
                delete copia[codigo];
            }else{
                copia[codigo]="1.00";
            }
            return copia;
        });
    }

    function alterarValorQuestao(codigo, valor){
        setSelecionadas((prev)=>({...prev, [codigo]:valor}));
    }

    function alternarTurma(codigoTurma){
        setTurmasMarcadas((prev)=>
            prev.includes(codigoTurma)?prev.filter((id)=>id!==codigoTurma):[...prev, codigoTurma]
        );
    }

    const somaSelecionadas=Object.values(selecionadas).reduce((acc, v)=>{
        const n=parseFloat(v);
        return acc+(isNaN(n)?0:n);
    }, 0);

    async function salvarAvaliacao(e){
        e.preventDefault();
        setErro("");
        setSucesso("");
        try{
            const itens=Object.entries(selecionadas).map(([codigoQuestao, valor])=>({
                codigoQuestao:Number(codigoQuestao),
                valor:Number(valor)
            }));
            const payload={
                titulo,
                materia,
                descricao,
                idProfessor:usuario.id,
                itens,
                turmasIds:turmasMarcadas
            };
            const criada=await api.criarAvaliacao(payload);
            setSucesso(`Avaliacao #${criada.codigo} cadastrada.`);
            setTitulo("");
            setDescricao("");
            setSelecionadas({});
            setTurmasMarcadas([]);
            await carregarTudo();
        }catch(err){
            setErro(err.message);
        }
    }

    async function adicionarQuestaoNaExistente(codigoAvaliacao){
        if(!novaQuestaoExtra){
            return;
        }
        setErro("");
        setSucesso("");
        try{
            await api.adicionarItemAvaliacao(codigoAvaliacao, {
                codigoQuestao:Number(novaQuestaoExtra),
                valor:Number(novoValorExtra)
            });
            setNovaQuestaoExtra("");
            await carregarTudo();
        }catch(err){
            setErro(err.message);
        }
    }

    async function removerQuestaoDaExistente(codigoAvaliacao, codigoQuestao){
        setErro("");
        try{
            await api.removerItemAvaliacao(codigoAvaliacao, codigoQuestao);
            await carregarTudo();
        }catch(err){
            setErro(err.message);
        }
    }

    async function alternarTurmaExistente(av, codigoTurma){
        setErro("");
        try{
            const atuais=av.turmasIds||[];
            const novas=atuais.includes(codigoTurma)
                ?atuais.filter((id)=>id!==codigoTurma)
                :[...atuais, codigoTurma];
            if(novas.length===0){
                setErro("A avaliacao precisa ter pelo menos uma turma destinataria.");
                return;
            }
            await api.definirTurmasAvaliacao(av.codigo, novas);
            await carregarTudo();
        }catch(err){
            setErro(err.message);
        }
    }

    const questoesFiltradas=questoes.filter((q)=>
        q.enunciado.toLowerCase().includes(filtroQuestao.toLowerCase())||String(q.codigo).includes(filtroQuestao)
    );

    return (
        <div>
            {erro&&<p><strong>Erro:</strong> {erro}</p>}
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Cadastrar avaliacao</legend>
                <form onSubmit={salvarAvaliacao}>
                    <p>
                        <label>
                            Titulo:<br />
                            <input
                                type="text"
                                size={50}
                                maxLength={50}
                                value={titulo}
                                onChange={(e)=>setTitulo(e.target.value)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <label>
                            Materia:<br />
                            <input
                                type="text"
                                size={50}
                                maxLength={50}
                                value={materia}
                                onChange={(e)=>setMateria(e.target.value)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <label>
                            Descricao:<br />
                            <textarea
                                rows={3}
                                cols={60}
                                value={descricao}
                                onChange={(e)=>setDescricao(e.target.value)}
                            />
                        </label>
                    </p>

                    <fieldset>
                        <legend>
                            Questoes da avaliacao - selecionadas: {Object.keys(selecionadas).length} | total: {somaSelecionadas.toFixed(2)} pts
                        </legend>
                        <p>
                            Buscar questao:{" "}
                            <input
                                type="text"
                                size={35}
                                value={filtroQuestao}
                                onChange={(e)=>setFiltroQuestao(e.target.value)}
                            />
                        </p>
                        <table border="1" cellPadding="4">
                            <thead>
                                <tr>
                                    <th>Usar</th>
                                    <th>Codigo</th>
                                    <th>Tipo</th>
                                    <th>Enunciado</th>
                                    <th>Valor</th>
                                </tr>
                            </thead>
                            <tbody>
                                {questoesFiltradas.map((q)=>{
                                    const marcada=selecionadas[q.codigo]!==undefined;
                                    return (
                                        <tr key={q.codigo}>
                                            <td>
                                                <input
                                                    type="checkbox"
                                                    checked={marcada}
                                                    onChange={()=>alternarQuestao(q.codigo)}
                                                />
                                            </td>
                                            <td>{q.codigo}</td>
                                            <td>{q.tipo}</td>
                                            <td>{q.enunciado}</td>
                                            <td>
                                                {marcada?(
                                                    <input
                                                        type="number"
                                                        step="0.25"
                                                        min="0.25"
                                                        value={selecionadas[q.codigo]}
                                                        onChange={(e)=>alterarValorQuestao(q.codigo, e.target.value)}
                                                    />
                                                ):"-"}
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </fieldset>

                    <br />

                    <fieldset>
                        <legend>Turmas destinatarias</legend>
                        {turmas.map((t)=>(
                            <label key={t.codigo}>
                                <input
                                    type="checkbox"
                                    checked={turmasMarcadas.includes(t.codigo)}
                                    onChange={()=>alternarTurma(t.codigo)}
                                />
                                {t.nome} ({t.anoLetivo}){" "}
                            </label>
                        ))}
                    </fieldset>

                    <p>
                        <button type="submit">Salvar avaliacao</button>
                    </p>
                </form>
            </fieldset>

            <br />

            <fieldset>
                <legend>Avaliacoes cadastradas ({avaliacoes.length})</legend>
                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Titulo</th>
                            <th>Materia</th>
                            <th>Professor</th>
                            <th>Turmas</th>
                            <th>Valor total</th>
                            <th>Acao</th>
                        </tr>
                    </thead>
                    <tbody>
                        {avaliacoes.map((av)=>(
                            <React.Fragment key={av.codigo}>
                                <tr>
                                    <td>{av.codigo}</td>
                                    <td>{av.titulo}</td>
                                    <td>{av.materia}</td>
                                    <td>{av.nomeProfessor||"-"}</td>
                                    <td>{av.turmasNomes.join(", ")}</td>
                                    <td>{Number(av.valorTotal).toFixed(2)} ({av.itens.length} q)</td>
                                    <td>
                                        <button
                                            type="button"
                                            onClick={()=>setExpandida(expandida===av.codigo?null:av.codigo)}
                                        >
                                            {expandida===av.codigo?"Fechar":"Gerenciar"}
                                        </button>
                                    </td>
                                </tr>
                                {expandida===av.codigo&&(
                                    <tr>
                                        <td colSpan={7}>
                                            <p><strong>Descricao:</strong> {av.descricao||"-"}</p>
                                            <p>
                                                <strong>Turmas:</strong>{" "}
                                                {turmas.map((t)=>(
                                                    <label key={t.codigo}>
                                                        <input
                                                            type="checkbox"
                                                            checked={av.turmasIds.includes(t.codigo)}
                                                            onChange={()=>alternarTurmaExistente(av, t.codigo)}
                                                        />
                                                        {t.nome}{" "}
                                                    </label>
                                                ))}
                                            </p>
                                            <p><strong>Questoes:</strong></p>
                                            <ul>
                                                {av.itens.map((it)=>(
                                                    <li key={it.codigoQuestao}>
                                                        Q#{it.codigoQuestao} ({Number(it.valor).toFixed(2)} pts) - {it.enunciado}{" "}
                                                        <button
                                                            type="button"
                                                            onClick={()=>removerQuestaoDaExistente(av.codigo, it.codigoQuestao)}
                                                        >
                                                            Remover
                                                        </button>
                                                    </li>
                                                ))}
                                            </ul>
                                            <p>
                                                <select
                                                    value={novaQuestaoExtra}
                                                    onChange={(e)=>setNovaQuestaoExtra(e.target.value)}
                                                >
                                                    <option value="">Selecionar questao para adicionar...</option>
                                                    {questoes.map((q)=>(
                                                        <option key={q.codigo} value={q.codigo}>
                                                            #{q.codigo} ({q.tipo}) - {q.enunciado.slice(0, 60)}
                                                        </option>
                                                    ))}
                                                </select>{" "}
                                                Valor:{" "}
                                                <input
                                                    type="number"
                                                    step="0.25"
                                                    min="0.25"
                                                    value={novoValorExtra}
                                                    onChange={(e)=>setNovoValorExtra(e.target.value)}
                                                />{" "}
                                                <button type="button" onClick={()=>adicionarQuestaoNaExistente(av.codigo)}>
                                                    Vincular questao
                                                </button>
                                            </p>
                                        </td>
                                    </tr>
                                )}
                            </React.Fragment>
                        ))}
                    </tbody>
                </table>
            </fieldset>
        </div>
    );
}
