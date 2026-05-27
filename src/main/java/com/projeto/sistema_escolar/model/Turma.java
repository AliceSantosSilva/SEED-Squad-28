package com.projeto.sistema_escolar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "turmas")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String serie;
    private Integer ano;

    @ManyToOne
    @JoinColumn(name = "escola_id")
    private Escola escola;

    @OneToMany(mappedBy = "turma")
    @JsonIgnore
    private List<Usuario> alunos;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }

    public Escola getEscola() { return escola; }
    public void setEscola(Escola escola) { this.escola = escola; }

    public List<Usuario> getAlunos() { return alunos; }
    public void setAlunos(List<Usuario> alunos) { this.alunos = alunos; }
}