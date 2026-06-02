package com.projeto.sistema_escolar.dto;

import com.projeto.sistema_escolar.model.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponseDTO {
    private Integer id;
    private String nome;
    private String email;
    private String perfil;
    private String turma;
    private Boolean ativo;
    private LocalDateTime criadoEm;

    // Construtor a partir da entidade
    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.perfil = usuario.getPerfil() != null ? usuario.getPerfil().getNome() : null;
        this.turma = usuario.getTurma() != null ? usuario.getTurma().getNome() : null;
        this.ativo = usuario.getAtivo();
        this.criadoEm = usuario.getCriadoEm();
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getTurma() { return turma; }
    public void setTurma(String turma) { this.turma = turma; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}