package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "series")
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer nivelEnsino;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getNivelEnsino() { return nivelEnsino; }
    public void setNivelEnsino(Integer nivelEnsino) { this.nivelEnsino = nivelEnsino; }
}