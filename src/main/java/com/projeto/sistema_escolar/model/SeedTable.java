package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seed_tables")
public class SeedTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    private Boolean executada;

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Boolean getExecutada() {
        return executada;
    }

    public void setExecutada(Boolean executada) {
        this.executada = executada;
    }
}