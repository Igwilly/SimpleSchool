import React, {useEffect, useState} from "react";
import {api} from "../api.js";

const MIN_PROVA=0;
const MAX_PROVA=100;

export default function AvaliacoesPage({usuario, notificar}){
    const [avaliacoes, setAvaliacoes]=useState([]);
    const [questoes, setQuestoes]=useState([]);
    const [turmas, setTurmas]=useState([]);
    const [titulo, setTitulo]=useState("");
    const [materiaOpcao, setMateriaOpcao]=useState("");
    const [materiaNova, setMateriaNova]=useState("");
    const [dataAvaliacao, setDataAvaliacao]=useState("2026-11-27");
    const [horaInicio, setHoraInicio]=useState("19:15");
    const [horaFim, setHoraFim]=useState("21:00");
    const [descricao, setDescricao]=useState("");
    const [selecionadas, setSelecionadas]=useState({});
    const [turmasMarcadas, setTurmasMarcadas]=useState([]);
    const [filtroQuestao, setFiltroQuestao]=useState("");
    const [filtroTipoQuestao, setFiltroTipoQuestao]=useState("TODOS");
    const [filtroTurmaLista, setFiltroTurmaLista]=useState("");
    const [expandida, setExpandida]=useState(null);
    const [novaQuestaoExtra, setNovaQuestaoExtra]=useState("");
    const [novoValorExtra, setNovoValorExtra]=useState("1.00");
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
            const mats=[...new Set(avs.map((a)=>a.materia).filter(Boolean))];
            if(!materiaOpcao&&mats.length>0){
                setMateriaOpcao(mats[0]);
            }
        }catch(err){
            notificar("Erro ao carregar dados", err.message);
        }
    }

    useEffect(()=>{
        carregarTudo();
    }, []);

    const materiasExistentes=[...new Set(avaliacoes.map((a)=>a.materia).filter(Boolean))];

    function alterarTextoLimitado(valor, setter, nomeCampo, max){
        if(valor.length>max){
            setter(valor.slice(0, max));
            notificar("Limite atingido", `${nomeCampo} aceita no maximo ${max} caracteres.`);
            return;
        }
        setter(valor);
    }

    function somarMapa(mapa, ignorarCodigo=null){
        return Object.entries(mapa).reduce((acc, [cod, val])=>{
            if(ignorarCodigo!==null&&Number(cod)===Number(ignorarCodigo)){
                return acc;
            }
            const n=parseFloat(val);
            return acc+(isNaN(n)?0:n);
        }, 0);
    }

    function alternarQuestao(codigo){
        setSelecionadas((prev)=>{
            const copia={...prev};
            if(copia[codigo]!==undefined){
                delete copia[codigo];
            }else{
                const somaAtual=somarMapa(copia);
                const disponivel=Math.max(0, Number((MAX_PROVA-somaAtual).toFixed(2)));
                if(disponivel<=0){
                    copia[codigo]="0.00";
                    notificar("Limite da avaliacao", "A avaliacao ja atingiu o valor maximo de 100.00. A questao entrou com valor 0.00.");
                }else{
                    copia[codigo]=Math.min(1, disponivel).toFixed(2);
                }
            }
            return copia;
        });
    }

    function alterarValorQuestao(codigo, texto){
        if(texto===""){
            setSelecionadas((prev)=>({...prev, [codigo]:""}));
            return;
        }
        const n=Number(texto);
        if(isNaN(n)){
            return;
        }
        if(n<MIN_PROVA){
            setSelecionadas((prev)=>({...prev, [codigo]:"0.00"}));
            notificar("Valor limitado", "O valor minimo permitido e 0.00. Ajustado automaticamente.");
            return;
        }
        const somaOutras=somarMapa(selecionadas, codigo);
        const tetoQuestao=Math.max(0, Number((MAX_PROVA-somaOutras).toFixed(2)));
        if(n>tetoQuestao){
            setSelecionadas((prev)=>({...prev, [codigo]:tetoQuestao.toFixed(2)}));
            notificar(
                "Limite da avaliacao",
                `O valor total da avaliacao tem maximo de 100.00. O campo foi limitado a ${tetoQuestao.toFixed(2)}.`
            );
            return;
        }
        setSelecionadas((prev)=>({...prev, [codigo]:texto}));
    }

    function sairCampoValorQuestao(codigo){
        setSelecionadas((prev)=>{
            const atual=prev[codigo];
            const n=Number(atual);
            if(atual===""||isNaN(n)||n<MIN_PROVA){
                return {...prev, [codigo]:"0.00"};
            }
            return prev;
        });
    }

    function alterarValorExtra(texto, av){
        if(texto===""){
            setNovoValorExtra("");
            return;
        }
        const n=Number(texto);
        if(isNaN(n)){
            return;
        }
        if(n<MIN_PROVA){
            setNovoValorExtra("0.00");
            notificar("Valor limitado", "O valor minimo permitido e 0.00. Ajustado automaticamente.");
            return;
        }
        const itemExistente=av.itens.find((i)=>i.codigoQuestao===Number(novaQuestaoExtra));
        const baseAtual=Number(av.valorTotal)-(itemExistente?Number(itemExistente.valor):0);
        const teto=Math.max(0, Number((MAX_PROVA-baseAtual).toFixed(2)));
        if(n>teto){
            setNovoValorExtra(teto.toFixed(2));
            notificar(
                "Limite da avaliacao",
                `O valor total da avaliacao tem maximo de 100.00. O campo foi limitado a ${teto.toFixed(2)}.`
            );
            return;
        }
        setNovoValorExtra(texto);
    }

    function alternarTurma(codigoTurma){
        setTurmasMarcadas((prev)=>
            prev.includes(codigoTurma)?prev.filter((id)=>id!==codigoTurma):[...prev, codigoTurma]
        );
    }

    const somaSelecionadas=somarMapa(selecionadas);

    async function salvarAvaliacao(e){
        e.preventDefault();
        setSucesso("");
        if(horaFim<=horaInicio){
            notificar("Horario invalido", "O horario de fim deve ser posterior ao horario de inicio.");
            return;
        }
        if(somaSelecionadas<MIN_PROVA||somaSelecionadas>MAX_PROVA){
            notificar("Limite da avaliacao", "O valor total da avaliacao deve ficar entre 0.00 e 100.00.");
            return;
        }
        try{
            const materiaFinal=materiaOpcao==="__OUTRA__"?materiaNova:materiaOpcao;
            const itens=Object.entries(selecionadas).map(([codigoQuestao, valor])=>{
                const n=Number(valor);
                return {
                    codigoQuestao:Number(codigoQuestao),
                    valor:(valor===""||isNaN(n)||n<0)?0:n
                };
            });
            const payload={
                titulo,
                materia:materiaFinal,
                data:dataAvaliacao,
                horaInicio,
                horaFim,
                descricao,
                idProfessor:usuario.id,
                itens,
                turmasIds:turmasMarcadas
            };
            const criada=await api.criarAvaliacao(payload);
            setSucesso(`Avaliacao #${criada.codigo} cadastrada.`);
            setTitulo("");
            setMateriaNova("");
            setDescricao("");
            setSelecionadas({});
            setTurmasMarcadas([]);
            await carregarTudo();
        }catch(err){
            notificar("Erro ao salvar avaliacao", err.message);
        }
    }

    async function adicionarQuestaoNaExistente(av){
        if(!novaQuestaoExtra){
            notificar("Atencao", "Selecione uma questao no menu antes de vincular.");
            return;
        }
        setSucesso("");
        try{
            const bruto=Number(novoValorExtra);
            const itemExistente=av.itens.find((i)=>i.codigoQuestao===Number(novaQuestaoExtra));
            const baseAtual=Number(av.valorTotal)-(itemExistente?Number(itemExistente.valor):0);
            const teto=Math.max(0, Number((MAX_PROVA-baseAtual).toFixed(2)));
            const valFinal=isNaN(bruto)||bruto<0?0:Math.min(bruto, teto);
            await api.adicionarItemAvaliacao(av.codigo, {
                codigoQuestao:Number(novaQuestaoExtra),
                valor:valFinal
            });
            setNovaQuestaoExtra("");
            await carregarTudo();
        }catch(err){
            notificar("Erro ao vincular questao", err.message);
        }
    }

    async function removerQuestaoDaExistente(codigoAvaliacao, codigoQuestao){
        try{
            await api.removerItemAvaliacao(codigoAvaliacao, codigoQuestao);
            await carregarTudo();
        }catch(err){
            notificar("Erro ao remover questao", err.message);
        }
    }

    async function alternarTurmaExistente(av, codigoTurma){
        try{
            const atuais=av.turmasIds||[];
            const novas=atuais.includes(codigoTurma)
                ?atuais.filter((id)=>id!==codigoTurma)
                :[...atuais, codigoTurma];
            if(novas.length===0){
                notificar("Limitacao", "A avaliacao precisa ter pelo menos uma turma destinataria.");
                return;
            }
            await api.definirTurmasAvaliacao(av.codigo, novas);
            await carregarTudo();
        }catch(err){
            notificar("Erro ao atualizar turmas", err.message);
        }
    }

    const questoesFiltradas=questoes.filter((q)=>{
        const bateTipo=filtroTipoQuestao==="TODOS"||q.tipo===filtroTipoQuestao;
        const bateTexto=q.enunciado.toLowerCase().includes(filtroQuestao.toLowerCase())||String(q.codigo).includes(filtroQuestao);
        return bateTipo&&bateTexto;
    });

    const avaliacoesFiltradas=avaliacoes.filter((av)=>
        !filtroTurmaLista||av.turmasIds.includes(Number(filtroTurmaLista))
    );

    return (
        <div>
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Cadastrar avaliacao</legend>
                <form onSubmit={salvarAvaliacao}>
                    <p>
                        <label>
                            Titulo (max 50 caracteres):<br />
                            <input
                                type="text"
                                size={50}
                                placeholder="Titulo da avaliacao (max 50)"
                                value={titulo}
                                onChange={(e)=>alterarTextoLimitado(e.target.value, setTitulo, "O titulo", 50)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <label>
                            Materia:{" "}
                            <select
                                value={materiaOpcao}
                                onChange={(e)=>setMateriaOpcao(e.target.value)}
                                required
                            >
                                {materiasExistentes.map((m)=>(
                                    <option key={m} value={m}>{m}</option>
                                ))}
                                <option value="__OUTRA__">Outra materia...</option>
                            </select>
                        </label>{" "}
                        {materiaOpcao==="__OUTRA__"&&(
                            <input
                                type="text"
                                size={32}
                                placeholder="Nova materia (max 50)"
                                value={materiaNova}
                                onChange={(e)=>alterarTextoLimitado(e.target.value, setMateriaNova, "A materia", 50)}
                                required
                            />
                        )}
                    </p>
                    <p>
                        <label>
                            Data:{" "}
                            <input
                                type="date"
                                value={dataAvaliacao}
                                onChange={(e)=>setDataAvaliacao(e.target.value)}
                                required
                            />
                        </label>{" | "}
                        <label>
                            Horario de inicio:{" "}
                            <input
                                type="time"
                                value={horaInicio}
                                onChange={(e)=>setHoraInicio(e.target.value)}
                                required
                            />
                        </label>{" | "}
                        <label>
                            Horario de fim:{" "}
                            <input
                                type="time"
                                value={horaFim}
                                onChange={(e)=>setHoraFim(e.target.value)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <label>
                            Descricao (opcional):<br />
                            <textarea
                                rows={3}
                                cols={60}
                                placeholder="Instrucoes ou conteudo da avaliacao (texto livre)"
                                value={descricao}
                                onChange={(e)=>setDescricao(e.target.value)}
                            />
                        </label>
                    </p>

                    <fieldset>
                        <legend>
                            Questoes da avaliacao - selecionadas: {Object.keys(selecionadas).length} | total: {somaSelecionadas.toFixed(2)} / 100.00 pts (min 0, max 100)
                        </legend>
                        <p>
                            <label>
                                Tipo:{" "}
                                <select value={filtroTipoQuestao} onChange={(e)=>setFiltroTipoQuestao(e.target.value)}>
                                    <option value="TODOS">Todos</option>
                                    <option value="OBJETIVA">Objetiva</option>
                                    <option value="DISSERTATIVA">Dissertativa</option>
                                </select>
                            </label>{" "}
                            <label>
                                Buscar questao:{" "}
                                <input
                                    type="text"
                                    size={30}
                                    placeholder="Codigo ou texto"
                                    value={filtroQuestao}
                                    onChange={(e)=>setFiltroQuestao(e.target.value)}
                                />
                            </label>
                        </p>
                        <table border="1" cellPadding="4">
                            <thead>
                                <tr>
                                    <th>Usar</th>
                                    <th>Codigo</th>
                                    <th>Tipo</th>
                                    <th>Enunciado</th>
                                    <th>Valor (min 0, max 100 na prova)</th>
                                </tr>
                            </thead>
                            <tbody>
                                {questoesFiltradas.map((q)=>{
                                    const marcada=selecionadas[q.codigo]!==undefined;
                                    const tetoRestante=Math.max(0, Number((MAX_PROVA-somarMapa(selecionadas, q.codigo)).toFixed(2)));
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
                                                        min={MIN_PROVA}
                                                        max={tetoRestante}
                                                        placeholder={`0.00 a ${tetoRestante.toFixed(2)}`}
                                                        value={selecionadas[q.codigo]}
                                                        onChange={(e)=>alterarValorQuestao(q.codigo, e.target.value)}
                                                        onBlur={()=>sairCampoValorQuestao(q.codigo)}
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
                        <legend>Turmas destinatarias (selecione pelo menos 1)</legend>
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
                <legend>Avaliacoes cadastradas ({avaliacoesFiltradas.length})</legend>
                <p>
                    <label>
                        Filtrar por turma:{" "}
                        <select value={filtroTurmaLista} onChange={(e)=>setFiltroTurmaLista(e.target.value)}>
                            <option value="">Todas as turmas</option>
                            {turmas.map((t)=>(
                                <option key={t.codigo} value={t.codigo}>{t.nome}</option>
                            ))}
                        </select>
                    </label>
                </p>
                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Titulo</th>
                            <th>Materia</th>
                            <th>Data e horario</th>
                            <th>Professor</th>
                            <th>Turmas</th>
                            <th>Valor total</th>
                            <th>Acao</th>
                        </tr>
                    </thead>
                    <tbody>
                        {avaliacoesFiltradas.map((av)=>(
                            <React.Fragment key={av.codigo}>
                                <tr>
                                    <td>{av.codigo}</td>
                                    <td>{av.titulo}</td>
                                    <td>{av.materia}</td>
                                    <td>
                                        {av.data?`${av.data} (${av.horaInicio} as ${av.horaFim})`:"-"}
                                    </td>
                                    <td>{av.nomeProfessor||"-"}</td>
                                    <td>{av.turmasNomes.join(", ")}</td>
                                    <td>{Number(av.valorTotal).toFixed(2)} / 100.00 ({av.itens.length} q)</td>
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
                                        <td colSpan={8}>
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
                                                Valor (min 0, max {(MAX_PROVA-Number(av.valorTotal)).toFixed(2)} disp.):{" "}
                                                <input
                                                    type="number"
                                                    step="0.25"
                                                    min={MIN_PROVA}
                                                    max={Math.max(0, MAX_PROVA-Number(av.valorTotal))}
                                                    placeholder="0.00 a 100.00"
                                                    value={novoValorExtra}
                                                    onChange={(e)=>alterarValorExtra(e.target.value, av)}
                                                    onBlur={()=>{
                                                        if(novoValorExtra===""||isNaN(Number(novoValorExtra))||Number(novoValorExtra)<0){
                                                            setNovoValorExtra("0.00");
                                                        }
                                                    }}
                                                />{" "}
                                                <button type="button" onClick={()=>adicionarQuestaoNaExistente(av)}>
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
