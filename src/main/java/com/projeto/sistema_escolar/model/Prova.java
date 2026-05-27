package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "provas")
public class Prova {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private int duracaoMinutos;
    private String tipo; // SIMULADO, CERTIFICACAO
    private String modalidade; // ONLINE, PRESENCIAL
    private double notaMinimaAprovacao;
    private boolean gerarVariacoes;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToMany
    @JoinTable(
        name = "prova_questoes",
        joinColumns = @JoinColumn(name = "prova_id"),
        inverseJoinColumns = @JoinColumn(name = "questao_id")
    )
    private List<Questao> questoes = new ArrayList<>();

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public int getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(int duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public double getNotaMinimaAprovacao() { return notaMinimaAprovacao; }
    public void setNotaMinimaAprovacao(double notaMinimaAprovacao) { this.notaMinimaAprovacao = notaMinimaAprovacao; }

    public boolean isGerarVariacoes() { return gerarVariacoes; }
    public void setGerarVariacoes(boolean gerarVariacoes) { this.gerarVariacoes = gerarVariacoes; }

    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }

    public List<Questao> getQuestoes() { return questoes; }
    public void setQuestoes(List<Questao> questoes) { this.questoes = questoes; }
}