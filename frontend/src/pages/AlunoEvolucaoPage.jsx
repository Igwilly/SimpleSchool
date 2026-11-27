import React, {useEffect, useState} from "react";
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
} from "chart.js";
import {Line} from "react-chartjs-2";
import {api} from "../api.js";

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

export default function AlunoEvolucaoPage({usuario, notificar}){
    const [dados, setDados]=useState([]);

    useEffect(()=>{
        if(usuario?.id){
            api.relatorioEvolucaoAluno(usuario.id)
                .then(setDados)
                .catch((err)=>notificar("Erro ao carregar desempenho", err.message));
        }
    }, [usuario]);

    const grafico={
        labels:dados.map((d)=>`${d.data} (#${d.codigo})`),
        datasets:[
            {
                label:"Minha nota",
                data:dados.map((d)=>Number(d.notaAluno)),
                borderColor:"rgb(37, 99, 235)",
                backgroundColor:"rgba(37, 99, 235, 0.3)"
            },
            {
                label:"Media da avaliacao",
                data:dados.map((d)=>Number(d.mediaAvaliacao)),
                borderColor:"rgb(100, 116, 139)",
                backgroundColor:"rgba(100, 116, 139, 0.3)"
            }
        ]
    };

    return (
        <fieldset>
            <legend>Meu desempenho ao longo do tempo</legend>
            {dados.length===0?(
                <p>Voce ainda nao realizou avaliacoes.</p>
            ):(
                <div>
                    <Line
                        width={820}
                        height={300}
                        data={grafico}
                        options={{responsive:false}}
                    />
                    <br />
                    <table border="1" cellPadding="5">
                        <thead>
                            <tr>
                                <th>Data</th>
                                <th>Codigo</th>
                                <th>Titulo</th>
                                <th>Minha nota</th>
                                <th>Media da avaliacao</th>
                                <th>Diferenca</th>
                            </tr>
                        </thead>
                        <tbody>
                            {dados.map((d)=>{
                                const dif=Number(d.notaAluno)-Number(d.mediaAvaliacao);
                                return (
                                    <tr key={`${d.codigo}_${d.data}`}>
                                        <td>{d.data}</td>
                                        <td>#{d.codigo}</td>
                                        <td>{d.titulo}</td>
                                        <td><strong>{Number(d.notaAluno).toFixed(2)}</strong></td>
                                        <td>{Number(d.mediaAvaliacao).toFixed(2)}</td>
                                        <td>{dif>=0?`+${dif.toFixed(2)}`:dif.toFixed(2)}</td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </fieldset>
    );
}
