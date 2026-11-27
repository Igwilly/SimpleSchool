import React, {useEffect, useState} from "react";
import {api} from "../api.js";

export default function TurmasPage(){
    const [turmas, setTurmas]=useState([]);
    const [nome, setNome]=useState("");
    const [anoLetivo, setAnoLetivo]=useState(2026);
    const [erro, setErro]=useState("");
    const [sucesso, setSucesso]=useState("");

    async function carregar(){
        try{
            setTurmas(await api.listarTurmas());
        }catch(err){
            setErro(err.message);
        }
    }

    useEffect(()=>{
        carregar();
    }, []);

    async function salvar(e){
        e.preventDefault();
        setErro("");
        setSucesso("");
        try{
            const criada=await api.criarTurma({codigo:0, nome, anoLetivo:Number(anoLetivo)});
            setSucesso(`Turma #${criada.codigo} cadastrada.`);
            setNome("");
            await carregar();
        }catch(err){
            setErro(err.message);
        }
    }

    async function excluir(codigo){
        setErro("");
        setSucesso("");
        try{
            await api.excluirTurma(codigo);
            await carregar();
        }catch(err){
            setErro(err.message);
        }
    }

    return (
        <div>
            {erro&&<p><strong>Erro:</strong> {erro}</p>}
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Cadastrar turma</legend>
                <form onSubmit={salvar}>
                    <p>
                        <label>
                            Nome:{" "}
                            <input
                                type="text"
                                maxLength={50}
                                value={nome}
                                onChange={(e)=>setNome(e.target.value)}
                                required
                            />
                        </label>{" "}
                        <label>
                            Ano letivo:{" "}
                            <input
                                type="number"
                                min={2000}
                                value={anoLetivo}
                                onChange={(e)=>setAnoLetivo(e.target.value)}
                                required
                            />
                        </label>{" "}
                        <button type="submit">Salvar turma</button>
                    </p>
                </form>
            </fieldset>

            <br />

            <fieldset>
                <legend>Turmas cadastradas ({turmas.length})</legend>
                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Nome</th>
                            <th>Ano letivo</th>
                            <th>Acao</th>
                        </tr>
                    </thead>
                    <tbody>
                        {turmas.map((t)=>(
                            <tr key={t.codigo}>
                                <td>{t.codigo}</td>
                                <td>{t.nome}</td>
                                <td>{t.anoLetivo}</td>
                                <td>
                                    <button type="button" onClick={()=>excluir(t.codigo)}>Excluir</button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </fieldset>
        </div>
    );
}
