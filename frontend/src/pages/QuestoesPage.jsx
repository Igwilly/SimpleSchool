import React, {useEffect, useState} from "react";
import {api} from "../api.js";

const LETRAS=["A", "B", "C", "D", "E"];

function alternativasIniciais(){
    return [
        {letra:"A", texto:"", correta:true},
        {letra:"B", texto:"", correta:false}
    ];
}

export default function QuestoesPage(){
    const [questoes, setQuestoes]=useState([]);
    const [tipo, setTipo]=useState("OBJETIVA");
    const [enunciado, setEnunciado]=useState("");
    const [respostaEsperada, setRespostaEsperada]=useState("");
    const [alternativas, setAlternativas]=useState(alternativasIniciais());
    const [busca, setBusca]=useState("");
    const [erro, setErro]=useState("");
    const [sucesso, setSucesso]=useState("");

    async function carregar(){
        try{
            setQuestoes(await api.listarQuestoes());
        }catch(err){
            setErro(err.message);
        }
    }

    useEffect(()=>{
        carregar();
    }, []);

    function alterarTextoAlt(idx, texto){
        setAlternativas((prev)=>prev.map((a, i)=>(i===idx?{...a, texto}:a)));
    }

    function marcarCorreta(idx){
        setAlternativas((prev)=>prev.map((a, i)=>({...a, correta:i===idx})));
    }

    function adicionarAlternativa(){
        if(alternativas.length>=5){
            return;
        }
        setAlternativas((prev)=>[...prev, {letra:LETRAS[prev.length], texto:"", correta:false}]);
    }

    function removerAlternativa(idx){
        if(alternativas.length<=2){
            return;
        }
        setAlternativas((prev)=>{
            const filtradas=prev.filter((_, i)=>i!==idx).map((a, i)=>({...a, letra:LETRAS[i]}));
            if(!filtradas.some((a)=>a.correta)){
                filtradas[0].correta=true;
            }
            return filtradas;
        });
    }

    async function salvar(e){
        e.preventDefault();
        setErro("");
        setSucesso("");
        try{
            const payload={
                tipo,
                enunciado,
                respostaEsperada:tipo==="DISSERTATIVA"?respostaEsperada:null,
                alternativas:tipo==="OBJETIVA"?alternativas:[]
            };
            const criada=await api.criarQuestao(payload);
            setSucesso(`Questao #${criada.codigo} cadastrada.`);
            setEnunciado("");
            setRespostaEsperada("");
            setAlternativas(alternativasIniciais());
            await carregar();
        }catch(err){
            setErro(err.message);
        }
    }

    async function excluir(codigo){
        setErro("");
        setSucesso("");
        try{
            await api.excluirQuestao(codigo);
            setSucesso(`Questao #${codigo} removida.`);
            await carregar();
        }catch(err){
            setErro(err.message);
        }
    }

    const filtradas=questoes.filter((q)=>
        q.enunciado.toLowerCase().includes(busca.toLowerCase())||String(q.codigo).includes(busca)
    );

    return (
        <div>
            {erro&&<p><strong>Erro:</strong> {erro}</p>}
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Cadastrar questao</legend>
                <form onSubmit={salvar}>
                    <p>
                        <label>
                            Tipo:{" "}
                            <select value={tipo} onChange={(e)=>setTipo(e.target.value)}>
                                <option value="OBJETIVA">Objetiva</option>
                                <option value="DISSERTATIVA">Dissertativa</option>
                            </select>
                        </label>
                    </p>

                    <p>
                        <label>
                            Enunciado:<br />
                            <textarea
                                rows={3}
                                cols={70}
                                value={enunciado}
                                onChange={(e)=>setEnunciado(e.target.value)}
                                required
                            />
                        </label>
                    </p>

                    {tipo==="OBJETIVA"?(
                        <div>
                            <p>Alternativas (marque a correta):</p>
                            {alternativas.map((alt, idx)=>(
                                <p key={alt.letra}>
                                    <strong>{alt.letra})</strong>{" "}
                                    <input
                                        type="text"
                                        size={50}
                                        value={alt.texto}
                                        onChange={(e)=>alterarTextoAlt(idx, e.target.value)}
                                        required
                                    />{" "}
                                    <label>
                                        <input
                                            type="radio"
                                            name="alt_correta"
                                            checked={alt.correta}
                                            onChange={()=>marcarCorreta(idx)}
                                        />
                                        Correta
                                    </label>{" "}
                                    {alternativas.length>2&&(
                                        <button type="button" onClick={()=>removerAlternativa(idx)}>Remover</button>
                                    )}
                                </p>
                            ))}
                            {alternativas.length<5&&(
                                <p>
                                    <button type="button" onClick={adicionarAlternativa}>Adicionar alternativa</button>
                                </p>
                            )}
                        </div>
                    ):(
                        <p>
                            <label>
                                Resposta esperada:<br />
                                <textarea
                                    rows={3}
                                    cols={70}
                                    value={respostaEsperada}
                                    onChange={(e)=>setRespostaEsperada(e.target.value)}
                                    required
                                />
                            </label>
                        </p>
                    )}

                    <p>
                        <button type="submit">Salvar questao</button>
                    </p>
                </form>
            </fieldset>

            <br />

            <fieldset>
                <legend>Questoes cadastradas ({filtradas.length})</legend>
                <p>
                    Filtrar:{" "}
                    <input
                        type="text"
                        size={40}
                        value={busca}
                        onChange={(e)=>setBusca(e.target.value)}
                    />
                </p>

                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Tipo</th>
                            <th>Enunciado e detalhes</th>
                            <th>Acao</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtradas.map((q)=>(
                            <tr key={q.codigo}>
                                <td>{q.codigo}</td>
                                <td>{q.tipo}</td>
                                <td>
                                    <div>{q.enunciado}</div>
                                    {q.tipo==="OBJETIVA"?(
                                        <ul>
                                            {q.alternativas.map((a)=>(
                                                <li key={a.letra}>
                                                    {a.letra}) {a.texto} {a.correta?<strong>(correta)</strong>:null}
                                                </li>
                                            ))}
                                        </ul>
                                    ):(
                                        <div><em>Resposta esperada:</em> {q.respostaEsperada}</div>
                                    )}
                                </td>
                                <td>
                                    <button type="button" onClick={()=>excluir(q.codigo)}>Excluir</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </fieldset>
        </div>
    );
}
