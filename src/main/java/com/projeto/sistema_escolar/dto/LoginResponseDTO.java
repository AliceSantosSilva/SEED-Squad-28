package com.projeto.sistema_escolar.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Resposta do login.
 * O campo "token" só aparece no JSON quando não é nulo (login bem-sucedido).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponseDTO {

    private String  nome;
    private String  perfil;
    private String  mensagem;
    private boolean sucesso;
    private boolean senhaExpirada;
    private String  redirect;
    private String  token;       // JWT definitivo (login normal)
    private String  tokenTemp;   // JWT temporário (primeiro acesso)

    // ── Login bem-sucedido ────────────────────────────────────────────────────
    public LoginResponseDTO(String nome, String perfil, String mensagem,
                            boolean sucesso, boolean senhaExpirada,
                            String redirect, String token) {
        this.nome         = nome;
        this.perfil       = perfil;
        this.mensagem     = mensagem;
        this.sucesso      = sucesso;
        this.senhaExpirada = senhaExpirada;
        this.redirect     = redirect;
        this.token        = token;
    }

    // ── Só mensagem de erro ───────────────────────────────────────────────────
    public LoginResponseDTO(String mensagem) {
        this.mensagem     = mensagem;
        this.sucesso      = false;
        this.senhaExpirada = false;
    }

    // ── Senha expirada (primeiro acesso) ──────────────────────────────────────
    public LoginResponseDTO(String nome, String perfil, String mensagem,
                            boolean senhaExpirada, String tokenTemp) {
        this.nome         = nome;
        this.perfil       = perfil;
        this.mensagem     = mensagem;
        this.sucesso      = false;
        this.senhaExpirada = senhaExpirada;
        this.tokenTemp    = tokenTemp;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String  getNome()          { return nome; }
    public String  getPerfil()        { return perfil; }
    public String  getMensagem()      { return mensagem; }
    public boolean isSucesso()        { return sucesso; }
    public boolean isSenhaExpirada()  { return senhaExpirada; }
    public String  getRedirect()      { return redirect; }
    public String  getToken()         { return token; }
    public String  getTokenTemp()     { return tokenTemp; }
}