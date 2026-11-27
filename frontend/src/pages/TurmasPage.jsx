import React, {useEffect, useState} from "react";
import {api} from "../api.js";

const MIN_ANO=2000;
const MAX_ANO=2100;

export default function TurmasPage({notificar}){
    const [turmas, setTurmas]=useState([]);
    const [nome, setNome]=useState("");
    const [anoLetivo, setAnoLetivo]=useState("2026");
    const [sucesso, setSucesso]=useState("");

    async function carregar(){
        try{
            setTurmas(await api.listarTurmas());
        }catch(err){
            notificar("Erro ao carregar turmas", err.message);
        }
    }

    useEffect(()=>{
        carregar();
    }, []);

    function alterarNome(valor){
        if(valor.length>50){
            setNome(valor.slice(0, 50));
            notificar("Limite atingido", "O nome da turma aceita no maximo 50 caracteres.");
            return;
        }
        setNome(valor);
    }

    function alterarAno(valor){
        if(valor===""){
            setAnoLetivo("");
            return;
        }
        const n=Number(valor);
        if(isNaN(n)){
            return;
        }
        if(n>MAX_ANO){
            setAnoLetivo(String(MAX_ANO));
            notificar("Ano limitado", `O ano letivo maximo e ${MAX_ANO}. Ajustado automaticamente.`);
            return;
        }
        setAnoLetivo(valor);
    }

    function validarAnoAoSair(){
        const n=Number(anoLetivo);
        if(anoLetivo===""||isNaN(n)||n<MIN_ANO){
            setAnoLetivo(String(MIN_ANO));
            notificar("Ano limitado", `O ano letivo minimo permitido pelo banco e ${MIN_ANO}.`);
        }
    }

    async function salvar(e){
        e.preventDefault();
        setSucesso("");
        const anoNum=Number(anoLetivo);
        const anoFinal=(isNaN(anoNum)||anoNum<MIN_ANO)?MIN_ANO:(anoNum>MAX_ANO?MAX_ANO:anoNum);
        try{
            const criada=await api.criarTurma({codigo:0, nome, anoLetivo:anoFinal});
            setSucesso(`Turma #${criada.codigo} cadastrada.`);
            setNome("");
            await carregar();
        }catch(err){
            notificar("Erro ao salvar turma", err.message);
        }
    }

    async function excluir(codigo){
        setSucesso("");
        try{
            await api.excluirTurma(codigo);
            await carregar();
        }catch(err){
            notificar("Erro ao excluir turma", err.message);
        }
    }

    return (
        <div>
            {sucesso&&<p><strong>Ok:</strong> {sucesso}</p>}

            <fieldset>
                <legend>Cadastrar turma</legend>
                <form onSubmit={salvar}>
                    <p>
                        <label>
                            Nome (max 50 caracteres):{" "}
                            <input
                                type="text"
                                placeholder="Ex.: ES-B (max 50)"
                                value={nome}
                                onChange={(e)=>alterarNome(e.target.value)}
                                required
                            />
                        </label>{" "}
                        <label>
                            Ano letivo (min {MIN_ANO}, max {MAX_ANO}):{" "}
                            <input
                                type="number"
                                min={MIN_ANO}
                                max={MAX_ANO}
                                placeholder={`${MIN_ANO} a ${MAX_ANO}`}
                                value={anoLetivo}
                                onChange={(e)=>alterarAno(e.target.value)}
                                onBlur={validarAnoAoSair}
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
