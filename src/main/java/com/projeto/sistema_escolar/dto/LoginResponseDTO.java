package com.projeto.sistema_escolar.dto;

public class LoginResponseDTO {

    private String nome;
    private String perfil;
    private String mensagem;
    private boolean sucesso;
    private boolean senhaExpirada;
    private String redirect;  // ← campo novo: página de destino após login

    // ── Construtores ──────────────────────────────────────────────────────────

    /** Login bem-sucedido com redirecionamento */
    public LoginResponseDTO(String nome, String perfil, String mensagem,
                         boolean sucesso, boolean senhaExpirada, String redirect) {
        this.nome = nome;
        this.perfil = perfil;
        this.mensagem = mensagem;
        this.sucesso = sucesso;
        this.senhaExpirada = senhaExpirada;
        this.redirect = redirect;
    }

    /** Somente mensagem de erro */
    public LoginResponseDTO(String mensagem) {
        this.mensagem = mensagem;
        this.sucesso = false;
        this.senhaExpirada = false;
    }

    /** Senha expirada (primeiro acesso) */
    public LoginResponseDTO(String nome, String perfil, String mensagem, boolean senhaExpirada) {
        this.nome = nome;
        this.perfil = perfil;
        this.mensagem = mensagem;
        this.sucesso = false;
        this.senhaExpirada = senhaExpirada;
    }

    // ── Getters e Setters ─────────────────────────────────────────────────────

    public String getNome()                   { return nome; }
    public void setNome(String nome)          { this.nome = nome; }

    public String getPerfil()                 { return perfil; }
    public void setPerfil(String perfil)      { this.perfil = perfil; }

    public String getMensagem()               { return mensagem; }
    public void setMensagem(String mensagem)  { this.mensagem = mensagem; }

    public boolean isSucesso()                { return sucesso; }
    public void setSucesso(boolean sucesso)   { this.sucesso = sucesso; }

    public boolean isSenhaExpirada()                  { return senhaExpirada; }
    public void setSenhaExpirada(boolean senhaExpirada) { this.senhaExpirada = senhaExpirada; }

    public String getRedirect()               { return redirect; }
    public void setRedirect(String redirect)  { this.redirect = redirect; }
}