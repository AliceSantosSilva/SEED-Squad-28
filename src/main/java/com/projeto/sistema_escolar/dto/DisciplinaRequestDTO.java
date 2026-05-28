package com.projeto.sistema_escolar.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisciplinaRequestDTO {

    @NotBlank(message = "O nome da disciplina é obrigatório")
    private String nome;

}
