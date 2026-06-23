package com.projeto.sistema_escolar.dto;

public class QuestaoTrilhaDTO {
    private Integer id;
    private String enunciado;
    private Integer dificuldade;

    public QuestaoTrilhaDTO(Integer id, String enunciado, Integer dificuldade) {
        this.id = id;
        this.enunciado = enunciado;
        this.dificuldade = dificuldade;
    }

    public Integer getId() { return id; }
    public String getEnunciado() { return enunciado; }
    public Integer getDificuldade() { return dificuldade; }
}