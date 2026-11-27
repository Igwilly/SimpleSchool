import React from "react";

export default function AvisoLateral({aviso, onFechar}){
    if(!aviso){
        return null;
    }

    return (
        <div
            style={{
                position:"fixed",
                top:"16px",
                right:"16px",
                width:"310px",
                background:"#ffffff",
                zIndex:9999
            }}
        >
            <fieldset>
                <legend><strong>{aviso.titulo}</strong></legend>
                <p>{aviso.mensagem}</p>
                <p>
                    <button type="button" onClick={onFechar}>Fechar</button>
                </p>
            </fieldset>
        </div>
    );
}
