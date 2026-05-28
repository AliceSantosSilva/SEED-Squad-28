package com.projeto.sistema_escolar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestaoRequestDTO {

    @NotBlank(message = "O enunciado da questão é obrigatório")
    private String enunciado;

    @NotNull(message = "A dificuldade é obrigatória")
    private Integer dificuldade;

    @NotNull(message = "O ID da disciplina é obrigatório")
    private Long disciplinaId;

    @NotNull(message = "O ID da série é obrigatório")
    private Long serieId;

}