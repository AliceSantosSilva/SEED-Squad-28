package com.projeto.sistema_escolar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SerieRequestDTO {

    @NotBlank(message = "O nome da série é obrigatório")
    private String nome;

    @NotBlank(message = "O nível de ensino é obrigatório")
    private Integer nivelEnsino;
}