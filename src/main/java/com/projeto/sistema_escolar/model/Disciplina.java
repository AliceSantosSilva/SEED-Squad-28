package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "disciplinas")
@Data
public class Disciplina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nome;


}