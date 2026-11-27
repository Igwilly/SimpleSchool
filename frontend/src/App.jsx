import React, {useState} from "react";
import LoginPage from "./pages/LoginPage.jsx";
import QuestoesPage from "./pages/QuestoesPage.jsx";
import AvaliacoesPage from "./pages/AvaliacoesPage.jsx";
import TurmasPage from "./pages/TurmasPage.jsx";
import AlunoAvaliacoesPage from "./pages/AlunoAvaliacoesPage.jsx";

export default function App(){
    const [usuario, setUsuario]=useState(()=>{
        const salvo=localStorage.getItem("usuario_sessao");
        return salvo?JSON.parse(salvo):null;
    });
    const [aba, setAba]=useState("questoes");

    function salvarSessao(dados){
        localStorage.setItem("usuario_sessao", JSON.stringify(dados));
        setUsuario(dados);
        setAba(dados.tipo==="PROFESSOR"?"questoes":"aluno");
    }

    function sair(){
        localStorage.removeItem("usuario_sessao");
        setUsuario(null);
    }

    if(!usuario){
        return <LoginPage onLogin={salvarSessao} />;
    }

    const ehProfessor=usuario.tipo==="PROFESSOR";

    return (
        <div>
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
                    <button type="button" onClick={()=>setAba("turmas")} disabled={aba==="turmas"}>
                        Turmas
                    </button>
                </p>
            ):null}

            <hr />

            {ehProfessor&&aba==="questoes"&&<QuestoesPage />}
            {ehProfessor&&aba==="avaliacoes"&&<AvaliacoesPage usuario={usuario} />}
            {ehProfessor&&aba==="turmas"&&<TurmasPage />}
            {!ehProfessor&&<AlunoAvaliacoesPage usuario={usuario} />}
        </div>
    );
}
