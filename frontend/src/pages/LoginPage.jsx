import React, {useState} from "react";
import {api} from "../api.js";

export default function LoginPage({onLogin, notificar}){
    const [email, setEmail]=useState("");
    const [senha, setSenha]=useState("");
    const [carregando, setCarregando]=useState(false);

    function alterarEmail(valor){
        if(valor.length>50){
            setEmail(valor.slice(0, 50));
            notificar("Limite atingido", "O email suporta no maximo 50 caracteres.");
            return;
        }
        setEmail(valor);
    }

    async function submeter(e){
        e.preventDefault();
        setCarregando(true);
        try{
            const usuario=await api.login({email, senha});
            onLogin(usuario);
        }catch(err){
            notificar("Erro no login", err.message);
        }finally{
            setCarregando(false);
        }
    }

    return (
        <div>
            <h1>Sistema de avaliacoes</h1>
            <fieldset>
                <legend>Login</legend>
                <form onSubmit={submeter}>
                    <p>
                        <label>
                            Email (max 50 caracteres):<br />
                            <input
                                type="email"
                                size={35}
                                placeholder="usuario@gov.br (max 50)"
                                value={email}
                                onChange={(e)=>alterarEmail(e.target.value)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <label>
                            Senha:<br />
                            <input
                                type="password"
                                size={35}
                                placeholder="Digite sua senha"
                                value={senha}
                                onChange={(e)=>setSenha(e.target.value)}
                                required
                            />
                        </label>
                    </p>
                    <p>
                        <button type="submit" disabled={carregando}>
                            {carregando?"Entrando...":"Entrar"}
                        </button>
                    </p>
                </form>
            </fieldset>
        </div>
    );
}
