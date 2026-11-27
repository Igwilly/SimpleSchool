import React, {useEffect, useState} from "react";
import {api} from "../api.js";

export default function AlunoAvaliacoesPage({usuario}){
    const [avaliacoes, setAvaliacoes]=useState([]);
    const [erro, setErro]=useState("");

    useEffect(()=>{
        if(usuario?.turmaCodigo){
            api.listarAvaliacoesPorTurma(usuario.turmaCodigo)
                .then(setAvaliacoes)
                .catch((err)=>setErro(err.message));
        }
    }, [usuario]);

    return (
        <fieldset>
            <legend>Minhas avaliacoes destinadas</legend>
            {erro&&<p><strong>Erro:</strong> {erro}</p>}
            {avaliacoes.length===0?(
                <p>Nenhuma avaliacao destinada para sua turma no momento.</p>
            ):(
                <table border="1" cellPadding="5">
                    <thead>
                        <tr>
                            <th>Codigo</th>
                            <th>Titulo</th>
                            <th>Materia</th>
                            <th>Descricao</th>
                            <th>Questoes</th>
                            <th>Valor total</th>
                        </tr>
                    </thead>
                    <tbody>
                        {avaliacoes.map((av)=>(
                            <tr key={av.codigo}>
                                <td>{av.codigo}</td>
                                <td>{av.titulo}</td>
                                <td>{av.materia}</td>
                                <td>{av.descricao||"-"}</td>
                                <td>{av.itens.length}</td>
                                <td>{Number(av.valorTotal).toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </fieldset>
    );
}
