import React, {useState} from "react";
import AvisoLateral from "./AvisoLateral.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import QuestoesPage from "./pages/QuestoesPage.jsx";
import AvaliacoesPage from "./pages/AvaliacoesPage.jsx";
import TurmasPage from "./pages/TurmasPage.jsx";
import CorrecoesPage from "./pages/CorrecoesPage.jsx";
import RelatoriosPage from "./pages/RelatoriosPage.jsx";
import AlunoAvaliacoesPage from "./pages/AlunoAvaliacoesPage.jsx";
import AlunoEvolucaoPage from "./pages/AlunoEvolucaoPage.jsx";

export default function App(){
    const [usuario, setUsuario]=useState(()=>{
        const salvo=localStorage.getItem("usuario_sessao");
        return salvo?JSON.parse(salvo):null;
    });
    const [aba, setAba]=useState(()=>{
        const salvo=localStorage.getItem("usuario_sessao");
        const parsed=salvo?JSON.parse(salvo):null;
        return parsed?.tipo==="ALUNO"?"aluno":"questoes";
    });
    const [aviso, setAviso]=useState(null);

    function notificar(titulo, mensagem){
        setAviso({titulo, mensagem});
    }

    function salvarSessao(dados){
        localStorage.setItem("usuario_sessao", JSON.stringify(dados));
        setUsuario(dados);
        setAba(dados.tipo==="PROFESSOR"?"questoes":"aluno");
        setAviso(null);
    }

    function sair(){
        localStorage.removeItem("usuario_sessao");
        setUsuario(null);
        setAviso(null);
    }

    if(!usuario){
        return (
            <div>
                <AvisoLateral aviso={aviso} onFechar={()=>setAviso(null)} />
                <LoginPage onLogin={salvarSessao} notificar={notificar} />
            </div>
        );
    }

    const ehProfessor=usuario.tipo==="PROFESSOR";

    return (
        <div>
            <AvisoLateral aviso={aviso} onFechar={()=>setAviso(null)} />

            <h1>Sistema de avaliacoes</h1>
            <p>
                Usuario: <strong>{usuario.nome}</strong> ({usuario.tipo})
                {usuario.turmaCodigo?` - Turma #${usuario.turmaCodigo}`:""}{" "}
                <button type="button" onClick={sair}>Sair</button>
            </p>

            {ehProfessor?(
                <p>
                    <button type="button" onClick={()=>setAba("questoes")} disabled={aba==="questoes"}>
                        Questoes
                    </button>{" "}
                    <button type="button" onClick={()=>setAba("avaliacoes")} disabled={aba==="avaliacoes"}>
                        Avaliacoes
                    </button>{" "}
                    <button type="button" onClick={()=>setAba("correcoes")} disabled={aba==="correcoes"}>
                        Correcoes e notas
                    </button>{" "}
                    <button type="button" onClick={()=>setAba("relatorios")} disabled={aba==="relatorios"}>
                        Relatorios
                    </button>{" "}
                    <button type="button" onClick={()=>setAba("turmas")} disabled={aba==="turmas"}>
                        Turmas
                    </button>
                </p>
            ):(
                <p>
                    <button type="button" onClick={()=>setAba("aluno")} disabled={aba==="aluno"}>
                        Minhas avaliacoes
                    </button>{" "}
                    <button type="button" onClick={()=>setAba("evolucao")} disabled={aba==="evolucao"}>
                        Meu desempenho
                    </button>
                </p>
            )}

            <hr />

            {ehProfessor&&aba==="questoes"&&<QuestoesPage notificar={notificar} />}
            {ehProfessor&&aba==="avaliacoes"&&<AvaliacoesPage usuario={usuario} notificar={notificar} />}
            {ehProfessor&&aba==="correcoes"&&<CorrecoesPage usuario={usuario} notificar={notificar} />}
            {ehProfessor&&aba==="relatorios"&&<RelatoriosPage notificar={notificar} />}
            {ehProfessor&&aba==="turmas"&&<TurmasPage notificar={notificar} />}
            {!ehProfessor&&aba==="aluno"&&<AlunoAvaliacoesPage usuario={usuario} notificar={notificar} />}
            {!ehProfessor&&aba==="evolucao"&&<AlunoEvolucaoPage usuario={usuario} notificar={notificar} />}
        </div>
    );
}
