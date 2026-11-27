import React, {useEffect, useState} from "react";
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    BarElement,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
} from "chart.js";
import {Bar, Line} from "react-chartjs-2";
import {api} from "../api.js";

ChartJS.register(
    CategoryScale,
    LinearScale,
    BarElement,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
);

const MIN_RESP=1;
const MAX_RESP=500;

export default function RelatoriosPage({notificar}){
    const [abaRel, setAbaRel]=useState("avaliacoes");

    const [avaliacoesLista, setAvaliacoesLista]=useState([]);
    const [turmas, setTurmas]=useState([]);
    const [alunos, setAlunos]=useState([]);

    const [inicio, setInicio]=useState("2026-01-01");
    const [fim, setFim]=useState("2026-12-31");
    const [codAvaliacaoR1, setCodAvaliacaoR1]=useState("");
    const [codTurmaR1, setCodTurmaR1]=useState("");
    const [minRespondentes, setMinRespondentes]=useState("1");
    const [dadosAvaliacoes, setDadosAvaliacoes]=useState([]);

    const [limiteQuestoes, setLimiteQuestoes]=useState(10);
    const [termoQuestao, setTermoQuestao]=useState("");
    const [dadosQuestoes, setDadosQuestoes]=useState([]);

    const [turmaEvolucao, setTurmaEvolucao]=useState("");
    const [alunoEvolucao, setAlunoEvolucao]=useState("");
    const [dadosEvolucao, setDadosEvolucao]=useState([]);

    const [turmaPendenciaFiltro, setTurmaPendenciaFiltro]=useState("");
    const [dadosPendencias, setDadosPendencias]=useState({resumo:[], pendentes:[], questoesSemUso:[]});
    const [detalheLateral, setDetalheLateral]=useState(null);

    function alterarMinRespondentes(valor){
        if(valor===""){
            setMinRespondentes("");
            return;
        }
        const n=Number(valor);
        if(isNaN(n)){
            return;
        }
        if(n>MAX_RESP){
            setMinRespondentes(String(MAX_RESP));
            notificar("Valor limitado", `O minimo de respondentes foi limitado a ${MAX_RESP}.`);
            return;
        }
        if(n<MIN_RESP){
            setMinRespondentes(String(MIN_RESP));
            notificar("Valor limitado", `O minimo de respondentes deve ser pelo menos ${MIN_RESP}.`);
            return;
        }
        setMinRespondentes(valor);
    }

    async function carregarRelatorio1(paramsOverride){
        try{
            const minFinal=Number(minRespondentes)>=MIN_RESP?Number(minRespondentes):MIN_RESP;
            const p=paramsOverride||{
                inicio,
                fim,
                codigoAvaliacao:codAvaliacaoR1,
                codigoTurma:codTurmaR1,
                minRespondentes:minFinal
            };
            setDadosAvaliacoes(await api.relatorioAvaliacoes(p));
        }catch(err){
            notificar("Erro no relatorio", err.message);
        }
    }

    async function carregarRelatorio2(lim, termo){
        try{
            setDadosQuestoes(await api.relatorioRankingQuestoes(lim, termo));
        }catch(err){
            notificar("Erro no relatorio", err.message);
        }
    }

    async function carregarRelatorio3(codTurma, idAl){
        try{
            setDadosEvolucao(await api.relatorioEvolucao(codTurma||null, idAl||null));
        }catch(err){
            notificar("Erro no relatorio", err.message);
        }
    }

    async function carregarRelatorio4(codTurmaUnica, todasTurmas){
        try{
            const listaIds=codTurmaUnica
                ?[Number(codTurmaUnica)]
                :(todasTurmas||turmas).map((t)=>t.codigo);
            setDadosPendencias(await api.relatorioPendencias(listaIds));
            setDetalheLateral(null);
        }catch(err){
            notificar("Erro no relatorio", err.message);
        }
    }

    useEffect(()=>{
        carregarRelatorio1();
        carregarRelatorio2(10, "");
        carregarRelatorio3("", "");
        api.listarAvaliacoes().then(setAvaliacoesLista).catch((err)=>notificar("Erro", err.message));
        api.listarAlunosRelatorio().then(setAlunos).catch((err)=>notificar("Erro", err.message));
        api.listarTurmas()
            .then((lista)=>{
                setTurmas(lista);
                carregarRelatorio4("", lista);
            })
            .catch((err)=>notificar("Erro", err.message));
    }, []);

    function aoMudarTurmaEvolucao(novoCodTurma){
        setTurmaEvolucao(novoCodTurma);
        setAlunoEvolucao("");
        carregarRelatorio3(novoCodTurma, "");
    }

    function aoMudarAlunoEvolucao(novoIdAluno){
        setAlunoEvolucao(novoIdAluno);
        carregarRelatorio3(turmaEvolucao, novoIdAluno);
    }

    const alunosFiltradosPorTurma=turmaEvolucao
        ?alunos.filter((a)=>a.codigoTurma===Number(turmaEvolucao))
        :alunos;

    const rotuloLinhaPrincipal=alunoEvolucao
        ?"Nota do aluno"
        :turmaEvolucao
            ?"Media da turma"
            :"Media geral da avaliacao";

    const rotuloLinhaReferencia=alunoEvolucao||turmaEvolucao
        ?"Media da avaliacao"
        :"Media global do sistema";

    function alternarDetalheLateral(linhaResumo){
        const chave=`${linhaResumo.codigo}_${linhaResumo.codigoTurma}`;
        if(detalheLateral&&detalheLateral.chave===chave){
            setDetalheLateral(null);
        }else{
            setDetalheLateral({
                chave,
                codigoAvaliacao:linhaResumo.codigo,
                titulo:linhaResumo.titulo,
                codigoTurma:linhaResumo.codigoTurma,
                turma:linhaResumo.turma
            });
        }
    }

    const alunosDoDetalheLateral=detalheLateral
        ?dadosPendencias.pendentes.filter(
            (p)=>p.codigoAvaliacao===detalheLateral.codigoAvaliacao&&p.codigoTurma===detalheLateral.codigoTurma
        )
        :[];

    const graficoAvaliacoes={
        labels:dadosAvaliacoes.map((d)=>`#${d.codigo}`),
        datasets:[
            {
                label:"Media",
                data:dadosAvaliacoes.map((d)=>Number(d.mediaAvaliacao)),
                backgroundColor:"rgba(37, 99, 235, 0.7)"
            },
            {
                label:"Menor nota",
                data:dadosAvaliacoes.map((d)=>Number(d.menorNota)),
                backgroundColor:"rgba(239, 68, 68, 0.65)"
            },
            {
                label:"Maior nota",
                data:dadosAvaliacoes.map((d)=>Number(d.maiorNota)),
                backgroundColor:"rgba(22, 163, 74, 0.65)"
            }
        ]
    };

    const graficoQuestoes={
        labels:dadosQuestoes.map((q)=>`Q#${q.codigo}`),
        datasets:[
            {
                label:"Percentual de erro (%)",
                data:dadosQuestoes.map((q)=>Number(q.percentualErro)),
                backgroundColor:"rgba(220, 38, 38, 0.7)"
            },
            {
                label:"Percentual de acerto (%)",
                data:dadosQuestoes.map((q)=>Number(q.percentualAcerto)),
                backgroundColor:"rgba(37, 99, 235, 0.65)"
            }
        ]
    };

    const graficoEvolucao={
        labels:dadosEvolucao.map((d)=>`${d.data} (#${d.codigo})`),
        datasets:[
            {
                label:rotuloLinhaPrincipal,
                data:dadosEvolucao.map((d)=>Number(d.notaAluno)),
                borderColor:"rgb(37, 99, 235)",
                backgroundColor:"rgba(37, 99, 235, 0.3)"
            },
            {
                label:rotuloLinhaReferencia,
                data:dadosEvolucao.map((d)=>Number(d.mediaAvaliacao)),
                borderColor:"rgb(100, 116, 139)",
                backgroundColor:"rgba(100, 116, 139, 0.3)"
            }
        ]
    };

    const graficoPendencias={
        labels:dadosPendencias.resumo.map((r)=>`#${r.codigo} (${r.turma})`),
        datasets:[
            {
                label:"Responderam",
                data:dadosPendencias.resumo.map((r)=>Number(r.responderam)),
                backgroundColor:"rgba(22, 163, 74, 0.7)"
            },
            {
                label:"Pendentes",
                data:dadosPendencias.resumo.map((r)=>Number(r.pendentes)),
                backgroundColor:"rgba(234, 88, 12, 0.75)"
            }
        ]
    };

    return (
        <div>
            <p>
                <label>
                    Selecionar relatorio:{" "}
                    <select value={abaRel} onChange={(e)=>setAbaRel(e.target.value)}>
                        <option value="avaliacoes">Media por avaliacao</option>
                        <option value="questoes">Questoes com mais erros</option>
                        <option value="evolucao">Evolucao de desempenho</option>
                        <option value="pendencias">Adesao e pendentes</option>
                        <option value="sem_uso">Questoes nunca utilizadas</option>
                    </select>
                </label>
            </p>

            {abaRel==="avaliacoes"&&(
                <fieldset>
                    <legend>Media e distribuicao de notas por avaliacao</legend>
                    <form
                        onSubmit={(e)=>{
                            e.preventDefault();
                            carregarRelatorio1();
                        }}
                    >
                        <p>
                            <label>
                                Avaliacao (codigo):{" "}
                                <select
                                    value={codAvaliacaoR1}
                                    onChange={(e)=>{
                                        const v=e.target.value;
                                        setCodAvaliacaoR1(v);
                                        carregarRelatorio1({
                                            inicio,
                                            fim,
                                            codigoAvaliacao:v,
                                            codigoTurma:codTurmaR1,
                                            minRespondentes:Number(minRespondentes)||1
                                        });
                                    }}
                                >
                                    <option value="">Geral (todas)</option>
                                    {avaliacoesLista.map((av)=>(
                                        <option key={av.codigo} value={av.codigo}>
                                            #{av.codigo}
                                        </option>
                                    ))}
                                </select>
                            </label>{" | "}
                            <label>
                                Turma:{" "}
                                <select
                                    value={codTurmaR1}
                                    onChange={(e)=>{
                                        const v=e.target.value;
                                        setCodTurmaR1(v);
                                        carregarRelatorio1({
                                            inicio,
                                            fim,
                                            codigoAvaliacao:codAvaliacaoR1,
                                            codigoTurma:v,
                                            minRespondentes:Number(minRespondentes)||1
                                        });
                                    }}
                                >
                                    <option value="">Geral (todas as turmas)</option>
                                    {turmas.map((t)=>(
                                        <option key={t.codigo} value={t.codigo}>
                                            {t.nome}
                                        </option>
                                    ))}
                                </select>
                            </label>{" | "}
                            <label>
                                Data inicial:{" "}
                                <input type="date" value={inicio} onChange={(e)=>setInicio(e.target.value)} />
                            </label>{" "}
                            <label>
                                Data final:{" "}
                                <input type="date" value={fim} onChange={(e)=>setFim(e.target.value)} />
                            </label>{" "}
                            <label>
                                Minimo de respondentes (min {MIN_RESP}, max {MAX_RESP}):{" "}
                                <input
                                    type="number"
                                    min={MIN_RESP}
                                    max={MAX_RESP}
                                    placeholder={`${MIN_RESP} a ${MAX_RESP}`}
                                    value={minRespondentes}
                                    onChange={(e)=>alterarMinRespondentes(e.target.value)}
                                />
                            </label>{" "}
                            <button type="submit">Filtrar</button>
                        </p>
                    </form>

                    {dadosAvaliacoes.length===0?(
                        <p>Nenhum dado encontrado para os filtros informados.</p>
                    ):(
                        <div>
                            <Bar
                                width={820}
                                height={300}
                                data={graficoAvaliacoes}
                                options={{responsive:false}}
                            />
                            <br />
                            <table border="1" cellPadding="5">
                                <thead>
                                    <tr>
                                        <th>Codigo</th>
                                        <th>Titulo</th>
                                        <th>Respondentes</th>
                                        <th>Media</th>
                                        <th>Menor nota</th>
                                        <th>Maior nota</th>
                                        <th>Media geral do sistema</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dadosAvaliacoes.map((d)=>(
                                        <tr key={d.codigo}>
                                            <td>#{d.codigo}</td>
                                            <td>{d.titulo}</td>
                                            <td>{d.qtdRespondentes}</td>
                                            <td><strong>{Number(d.mediaAvaliacao).toFixed(2)}</strong></td>
                                            <td>{Number(d.menorNota).toFixed(2)}</td>
                                            <td>{Number(d.maiorNota).toFixed(2)}</td>
                                            <td>{Number(d.mediaGeralSistema).toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </fieldset>
            )}

            {abaRel==="questoes"&&(
                <fieldset>
                    <legend>Ranking das questoes com maior percentual de erro</legend>
                    <p>
                        <label>
                            Quantidade:{" "}
                            <select
                                value={limiteQuestoes}
                                onChange={(e)=>{
                                    const v=Number(e.target.value);
                                    setLimiteQuestoes(v);
                                    carregarRelatorio2(v, termoQuestao);
                                }}
                            >
                                <option value={5}>Top 5</option>
                                <option value={10}>Top 10</option>
                                <option value={15}>Top 15</option>
                            </select>
                        </label>{" | "}
                        <label>
                            Termo no enunciado:{" "}
                            <input
                                type="text"
                                placeholder="Palavra-chave opcional"
                                value={termoQuestao}
                                onChange={(e)=>setTermoQuestao(e.target.value)}
                            />
                        </label>{" "}
                        <button type="button" onClick={()=>carregarRelatorio2(limiteQuestoes, termoQuestao)}>
                            Filtrar
                        </button>
                    </p>

                    {dadosQuestoes.length===0?(
                        <p>Nenhuma questao encontrada.</p>
                    ):(
                        <div>
                            <Bar
                                width={820}
                                height={300}
                                data={graficoQuestoes}
                                options={{responsive:false}}
                            />
                            <br />
                            <table border="1" cellPadding="5">
                                <thead>
                                    <tr>
                                        <th>Posicao</th>
                                        <th>Codigo</th>
                                        <th>Enunciado</th>
                                        <th>Respostas</th>
                                        <th>Percentual de erro</th>
                                        <th>Percentual de acerto</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dadosQuestoes.map((q)=>(
                                        <tr key={q.codigo}>
                                            <td>{q.ranking}o</td>
                                            <td>#{q.codigo}</td>
                                            <td>{q.enunciado}</td>
                                            <td>{q.totalRespostas}</td>
                                            <td><strong>{Number(q.percentualErro).toFixed(1)}%</strong></td>
                                            <td>{Number(q.percentualAcerto).toFixed(1)}%</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </fieldset>
            )}

            {abaRel==="evolucao"&&(
                <fieldset>
                    <legend>Evolucao de desempenho ao longo do tempo</legend>
                    <p>
                        <label>
                            Turma:{" "}
                            <select
                                value={turmaEvolucao}
                                onChange={(e)=>aoMudarTurmaEvolucao(e.target.value)}
                            >
                                <option value="">Geral (todas as turmas)</option>
                                {turmas.map((t)=>(
                                    <option key={t.codigo} value={t.codigo}>
                                        {t.nome}
                                    </option>
                                ))}
                            </select>
                        </label>{" | "}
                        <label>
                            Aluno:{" "}
                            <select
                                value={alunoEvolucao}
                                onChange={(e)=>aoMudarAlunoEvolucao(e.target.value)}
                            >
                                <option value="">
                                    {turmaEvolucao?"Geral da turma (media)":"Geral (todos os alunos)"}
                                </option>
                                {alunosFiltradosPorTurma.map((a)=>(
                                    <option key={a.id} value={a.id}>
                                        {a.nome} ({a.turma})
                                    </option>
                                ))}
                            </select>
                        </label>
                    </p>

                    {dadosEvolucao.length===0?(
                        <p>Nenhuma avaliacao realizada para esta selecao.</p>
                    ):(
                        <div>
                            <Line
                                width={820}
                                height={300}
                                data={graficoEvolucao}
                                options={{responsive:false}}
                            />
                            <br />
                            <table border="1" cellPadding="5">
                                <thead>
                                    <tr>
                                        <th>Data</th>
                                        <th>Codigo</th>
                                        <th>Titulo</th>
                                        <th>{rotuloLinhaPrincipal}</th>
                                        <th>{rotuloLinhaReferencia}</th>
                                        <th>Diferenca</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {dadosEvolucao.map((d)=>{
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
            )}

            {abaRel==="pendencias"&&(
                <fieldset>
                    <legend>Adesao e pendentes por avaliacao</legend>
                    <p>
                        <label>
                            Turma:{" "}
                            <select
                                value={turmaPendenciaFiltro}
                                onChange={(e)=>{
                                    const v=e.target.value;
                                    setTurmaPendenciaFiltro(v);
                                    carregarRelatorio4(v);
                                }}
                            >
                                <option value="">Geral (todas as turmas)</option>
                                {turmas.map((t)=>(
                                    <option key={t.codigo} value={t.codigo}>
                                        {t.nome}
                                    </option>
                                ))}
                            </select>
                        </label>
                    </p>

                    {dadosPendencias.resumo.length>0&&(
                        <div>
                            <Bar
                                width={820}
                                height={300}
                                data={graficoPendencias}
                                options={{responsive:false}}
                            />
                            <br />
                            <table cellPadding="8">
                                <tbody>
                                    <tr>
                                        <td valign="top">
                                            <table border="1" cellPadding="5">
                                                <thead>
                                                    <tr>
                                                        <th>Codigo</th>
                                                        <th>Titulo</th>
                                                        <th>Turma</th>
                                                        <th>Destinados</th>
                                                        <th>Responderam</th>
                                                        <th>Pendentes</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {dadosPendencias.resumo.map((r)=>{
                                                        const chave=`${r.codigo}_${r.codigoTurma}`;
                                                        const aberto=detalheLateral?.chave===chave;
                                                        return (
                                                            <tr key={chave}>
                                                                <td>#{r.codigo}</td>
                                                                <td>{r.titulo}</td>
                                                                <td>{r.turma}</td>
                                                                <td>{r.totalDestinados}</td>
                                                                <td>{r.responderam}</td>
                                                                <td>
                                                                    <strong>{r.pendentes}</strong>{" "}
                                                                    {r.pendentes>0&&(
                                                                        <button
                                                                            type="button"
                                                                            onClick={()=>alternarDetalheLateral(r)}
                                                                        >
                                                                            {aberto?"<":">"}
                                                                        </button>
                                                                    )}
                                                                </td>
                                                            </tr>
                                                        );
                                                    })}
                                                </tbody>
                                            </table>
                                        </td>

                                        {detalheLateral&&(
                                            <td valign="top">
                                                <fieldset>
                                                    <legend>
                                                        Pendentes - #{detalheLateral.codigoAvaliacao} ({detalheLateral.turma})
                                                    </legend>
                                                    <table border="1" cellPadding="5">
                                                        <thead>
                                                            <tr>
                                                                <th>Aluno</th>
                                                                <th>Email</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {alunosDoDetalheLateral.map((p)=>(
                                                                <tr key={p.idAluno}>
                                                                    <td>{p.aluno}</td>
                                                                    <td>{p.email}</td>
                                                                </tr>
                                                            ))}
                                                        </tbody>
                                                    </table>
                                                </fieldset>
                                            </td>
                                        )}
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    )}
                </fieldset>
            )}

            {abaRel==="sem_uso"&&(
                <fieldset>
                    <legend>Questoes cadastradas nunca utilizadas ({dadosPendencias.questoesSemUso.length})</legend>
                    {dadosPendencias.questoesSemUso.length===0?(
                        <p>Todas as questoes cadastradas ja foram utilizadas em avaliacoes.</p>
                    ):(
                        <table border="1" cellPadding="5">
                            <thead>
                                <tr>
                                    <th>Codigo</th>
                                    <th>Tipo</th>
                                    <th>Enunciado</th>
                                </tr>
                            </thead>
                            <tbody>
                                {dadosPendencias.questoesSemUso.map((q)=>(
                                    <tr key={q.codigo}>
                                        <td>#{q.codigo}</td>
                                        <td>{q.tipo}</td>
                                        <td>{q.enunciado}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </fieldset>
            )}
        </div>
    );
}
