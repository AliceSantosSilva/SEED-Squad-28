package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "series")
@Data
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer nivelEnsino;


}