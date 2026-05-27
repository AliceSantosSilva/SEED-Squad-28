package com.projeto.sistema_escolar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "alternativas")
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String texto;

    private boolean correta;

    @ManyToOne
    @JoinColumn(name = "questao_id")
    @JsonIgnore
    private Questao questao;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isCorreta() { return correta; }
    public void setCorreta(boolean correta) { this.correta = correta; }

    public Questao getQuestao() { return questao; }
    public void setQuestao(Questao questao) { this.questao = questao; }
}