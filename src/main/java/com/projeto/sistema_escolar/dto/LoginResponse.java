package com.projeto.sistema_escolar.dto;

public class LoginResponse {
    private String nome;
    private String perfil;
    private String mensagem;
    private boolean sucesso;
    private boolean senhaExpirada;

    // Construtor para login bem-sucedido (5 parâmetros)
    public LoginResponse(String nome, String perfil, String mensagem, boolean sucesso, boolean senhaExpirada) {
        this.nome = nome;
        this.perfil = perfil;
        this.mensagem = mensagem;
        this.sucesso = sucesso;
        this.senhaExpirada = senhaExpirada;
    }

    // Construtor para erros (apenas mensagem)
    public LoginResponse(String mensagem) {
        this.mensagem = mensagem;
        this.sucesso = false;
        this.senhaExpirada = false;
    }

    // Construtor para erro de senha expirada (4 parâmetros)
    public LoginResponse(String nome, String perfil, String mensagem, boolean senhaExpirada) {
        this.nome = nome;
        this.perfil = perfil;
        this.mensagem = mensagem;
        this.sucesso = false;
        this.senhaExpirada = senhaExpirada;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public boolean isSenhaExpirada() { return senhaExpirada; }
    public void setSenhaExpirada(boolean senhaExpirada) { this.senhaExpirada = senhaExpirada; }
}