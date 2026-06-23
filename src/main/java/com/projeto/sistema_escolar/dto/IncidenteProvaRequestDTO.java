package com.projeto.sistema_escolar.dto;

public class IncidenteProvaRequestDTO {
    private String tipo;
    private String mensagem;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}