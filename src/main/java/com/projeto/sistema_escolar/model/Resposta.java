package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respostas")
public class Resposta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "aluno_id")
    private Usuario aluno;

    @ManyToOne
    @JoinColumn(name = "prova_id")
    private Prova prova;

    @ManyToOne
    @JoinColumn(name = "questao_id")
    private Questao questao;

    @ManyToOne
    @JoinColumn(name = "alternativa_id")
    private Alternativa alternativa;

    private Boolean correta;
    private LocalDateTime dataHoraResposta = LocalDateTime.now();

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Usuario getAluno() { return aluno; }
    public void setAluno(Usuario aluno) { this.aluno = aluno; }

    public Prova getProva() { return prova; }
    public void setProva(Prova prova) { this.prova = prova; }

    public Questao getQuestao() { return questao; }
    public void setQuestao(Questao questao) { this.questao = questao; }

    public Alternativa getAlternativa() { return alternativa; }
    public void setAlternativa(Alternativa alternativa) { this.alternativa = alternativa; }

    public Boolean getCorreta() { return correta; }
    public void setCorreta(Boolean correta) { this.correta = correta; }

    public LocalDateTime getDataHoraResposta() { return dataHoraResposta; }
    public void setDataHoraResposta(LocalDateTime dataHoraResposta) { this.dataHoraResposta = dataHoraResposta; }
}