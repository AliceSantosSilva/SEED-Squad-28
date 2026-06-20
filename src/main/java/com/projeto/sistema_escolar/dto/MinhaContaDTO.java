package com.projeto.sistema_escolar.dto;


public class MinhaContaDTO {

    private Integer id;
    private String nome;
    private String email;
    private String perfil;


    public MinhaContaDTO(
            Integer id,
            String nome,
            String email,
            String perfil) {

        this.id = id;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }


    public Integer getId() {
        return id;
    }


    public String getNome() {
        return nome;
    }


    public String getEmail() {
        return email;
    }


    public String getPerfil() {
        return perfil;
    }
}