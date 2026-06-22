package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    private String descricao;

    private LocalDate dataInicio;

    private LocalDate dataFim;

    // PERIODO_PROVA = período aberto pela coordenação
    // PROVA         = prova marcada pelo professor dentro de um período
    // EVENTO        = evento genérico da coordenação/admin
    private String tipo;

    @ManyToOne
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Usuario getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Usuario criadoPor) { this.criadoPor = criadoPor; }
}