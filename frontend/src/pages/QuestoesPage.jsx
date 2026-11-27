import React, {useEffect, useRef, useState} from "react";
import {api} from "../api.js";

const LETRAS=["A", "B", "C", "D", "E"];
const MAX_BYTES_IMAGEM=2*1024*1024;

function alternativasIniciais(){
    return [
        {letra:"A", texto:"", correta:true},
        {letra:"B", texto:"", correta:false}
    ];
}

export default function QuestoesPage({notificar}){
    const [questoes, setQuestoes]=useState([]);
    const [tipo, setTipo]=useState("OBJETIVA");
    const [enunciado, setEnunciado]=useState("");
    const [imagem, setImagem]=useState(null);
    const [respostaEsperada, setRespostaEsperada]=useState("");
    const [alternativas, setAlternativas]=useState(alternativasIniciais());
    const [busca, setBusca]=useState("");
    const [filtroTipo, setFiltroTipo]=useState("TODOS");
    const [sucesso, setSucesso]=useState("");
    const inputImagemRef=useRef(null);

    async function carregar(){
        try{
            setQuestoes(await api.listarQuestoes());
        }catch(err){
            notificar("Erro ao carregar questoes", err.message);
        }
    }

    useEffect(()=>{
        carregar();
    }, []);

    function selecionarImagem(e){
        const arquivo=e.target.files?.[0];
        if(!arquivo){
            setImagem(null);
            return;
        }
        if(!arquivo.type.startsWith("image/")){
            e.target.value="";
            setImagem(null);
            notificar("Arquivo invalido", "Selecione um arquivo de imagem (PNG, JPG, GIF ou WEBP).");
            return;
        }
        if(arquivo.size>MAX_BYTES_IMAGEM){
            e.target.value="";
            setImagem(null);
            notificar("Limite atingido", "A imagem deve ter no maximo 2 MB.");
            return;
        }
        const leitor=new FileReader();
        leitor.onload=()=>setImagem(String(leitor.result));
        leitor.onerror=()=>notificar("Erro", "Nao foi possivel ler a imagem selecionada.");
        leitor.readAsDataURL(arquivo);
    }

    function removerImagem(){
        setImagem(null);
        if(inputImagemRef.current){
            inputImagemRef.current.value="";
        }
    }

    function alterarTextoAlt(idx, texto){
        setAlternativas((prev)=>prev.map((a, i)=>(i===idx?{...a, texto}:a)));
    }

    function marcarCorretaPorLetra(letraEscolhida){
        setAlternativas((prev)=>prev.map((a)=>({...a, correta:a.letra===letraEscolhida})));
    }

    function alterarQuantidadeAlternativas(qtd){
        setAlternativas((prev)=>{
            const novaLista=[];
            for(let i=0; i<qtd; i++){
                novaLista.push(prev[i]?{...prev[i], letra:LETRAS[i]}:{letra:LETRAS[i], texto:"", correta:false});
            }
            if(!novaLista.some((a)=>a.correta)){
                novaLista[0].correta=true;
            }
            return novaLista;
        });
    }

    async function salvar(e){
        e.preventDefault();
        setSucesso("");
        try{
            const payload={
                tipo,
                enunciado,
                imagem:tipo==="OBJETIVA"?imagem:null,
                respostaEsperada:tipo==="DISSERTATIVA"?respostaEsperada:null,
                alternativas:tipo==="OBJETIVA"?alternativas:[]
            };
            const criada=await api.criarQuestao(payload);
            setSucesso(`Questao #${criada.codigo} cadastrada.`);
            setEnunciado("");
            removerImagem();
            setRespostaEsperada("");
            setAlternativas(alternativasIniciais());
            await carregar();
        }catch(err){
            notificar("Erro ao salvar questao", err.message);
        }
    }

    async function excluir(codigo){
        setSucesso("");
        try{
            await api.excluirQuestao(codigo);
            setSucesso(`Questao #${codigo} removida.`);
            await carregar();
        }catch(err){
            notificar("Erro ao excluir questao", err.message);
        }
    }

    const letraCorretaAtual=alternativas.find((a)=>a.correta)?.letra||"A";

    const filtradas=questoes.filter((q)=>{
        const bateTipo=filtroTipo==="TODOS"||q.tipo===filtroTipo;
        const bateBusca=q.enunciado.toLowerCase().includes(busca.toLowerCase())||String(q.codigo).includes(busca);
        return bateTipo&&bateBusca;
    });

    return (
        <div>
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
                            Enunciado (obrigatorio):<br />
                            <textarea
                                rows={3}
                                cols={70}
                                placeholder="Digite o enunciado da questao (texto livre)"
                                value={enunciado}
                                onChange={(e)=>setEnunciado(e.target.value)}
                                required
                            />
                        </label>
                    </p>

                    {tipo==="OBJETIVA"?(
                        <div>
                            <p>
                                <label>
                                    Imagem opcional (max 2 MB):{" "}
                                    <input
                                        ref={inputImagemRef}
                                        type="file"
                                        accept="image/*"
                                        onChange={selecionarImagem}
                                    />
                                </label>
                                {imagem&&(
                                    <span>
                                        {" "}
                                        <button type="button" onClick={removerImagem}>Remover imagem</button>
                                    </span>
                                )}
                            </p>
                            {imagem&&(
                                <p>
                                    <img src={imagem} alt="Previa da questao" width={220} />
                                </p>
                            )}

                            <p>
                                <label>
                                    Quantidade de alternativas (min 2, max 5):{" "}
                                    <select
                                        value={alternativas.length}
                                        onChange={(e)=>alterarQuantidadeAlternativas(Number(e.target.value))}
                                    >
                                        <option value={2}>2</option>
                                        <option value={3}>3</option>
                                        <option value={4}>4</option>
                                        <option value={5}>5</option>
                                    </select>
                                </label>{" | "}
                                <label>
                                    Alternativa correta:{" "}
                                    <select
                                        value={letraCorretaAtual}
                                        onChange={(e)=>marcarCorretaPorLetra(e.target.value)}
                                    >
                                        {alternativas.map((alt)=>(
                                            <option key={alt.letra} value={alt.letra}>{alt.letra}</option>
                                        ))}
                                    </select>
                                </label>
                            </p>
                            {alternativas.map((alt, idx)=>(
                                <p key={alt.letra}>
                                    <strong>{alt.letra})</strong>{" "}
                                    <input
                                        type="text"
                                        size={55}
                                        placeholder={`Texto da alternativa ${alt.letra} (obrigatorio)`}
                                        value={alt.texto}
                                        onChange={(e)=>alterarTextoAlt(idx, e.target.value)}
                                        required
                                    />
                                </p>
                            ))}
                        </div>
                    ):(
                        <p>
                            <label>
                                Resposta esperada (obrigatorio):<br />
                                <textarea
                                    rows={3}
                                    cols={70}
                                    placeholder="Digite a resposta esperada para orientar a correcao"
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
                    <label>
                        Tipo:{" "}
                        <select value={filtroTipo} onChange={(e)=>setFiltroTipo(e.target.value)}>
                            <option value="TODOS">Todos</option>
                            <option value="OBJETIVA">Objetiva</option>
                            <option value="DISSERTATIVA">Dissertativa</option>
                        </select>
                    </label>{" "}
                    <label>
                        Filtrar:{" "}
                        <input
                            type="text"
                            size={35}
                            placeholder="Codigo ou trecho do enunciado"
                            value={busca}
                            onChange={(e)=>setBusca(e.target.value)}
                        />
                    </label>
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
                                    {q.imagem&&(
                                        <div>
                                            <img src={q.imagem} alt={`Imagem Q#${q.codigo}`} width={220} />
                                        </div>
                                    )}
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
