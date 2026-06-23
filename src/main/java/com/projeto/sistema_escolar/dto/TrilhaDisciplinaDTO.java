package com.projeto.sistema_escolar.dto;

import java.util.List;

public class TrilhaDisciplinaDTO {
    private Integer disciplinaId;
    private String disciplinaNome;
    private double percentualAcerto;
    private String nivel;
    private List<QuestaoTrilhaDTO> questoesRecomendadas;

    public TrilhaDisciplinaDTO(Integer disciplinaId, String disciplinaNome, double percentualAcerto,
                                String nivel, List<QuestaoTrilhaDTO> questoesRecomendadas) {
        this.disciplinaId = disciplinaId;
        this.disciplinaNome = disciplinaNome;
        this.percentualAcerto = percentualAcerto;
        this.nivel = nivel;
        this.questoesRecomendadas = questoesRecomendadas;
    }

    public Integer getDisciplinaId() { return disciplinaId; }
    public String getDisciplinaNome() { return disciplinaNome; }
    public double getPercentualAcerto() { return percentualAcerto; }
    public String getNivel() { return nivel; }
    public List<QuestaoTrilhaDTO> getQuestoesRecomendadas() { return questoesRecomendadas; }
}