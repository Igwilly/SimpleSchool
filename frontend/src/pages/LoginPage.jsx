import React, {useState} from "react";
import {api} from "../api.js";

export default function LoginPage({onLogin}){
    const [email, setEmail]=useState("");
    const [senha, setSenha]=useState("");
    const [erro, setErro]=useState("");
    const [carregando, setCarregando]=useState(false);

    async function submeter(e){
        e.preventDefault();
        setErro("");
        setCarregando(true);
        try{
            const usuario=await api.login({email, senha});
            onLogin(usuario);
        }catch(err){
            setErro(err.message);
        }finally{
            setCarregando(false);
        }
    }

    return (
        <div>
            <h1>Sistema de avaliacoes</h1>
            <fieldset>
                <legend>Login</legend>
                {erro&&<p><strong>Erro:</strong> {erro}</p>}
                <form onSubmit={submeter}>
                    <p>
                        <label>
                            Email:<br />
                            <input
                                type="email"
                                size={35}
                                value={email}
                                onChange={(e)=>setEmail(e.target.value)}
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
