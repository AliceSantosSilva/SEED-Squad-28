package com.projeto.sistema_escolar.model;

import jakarta.persistence.*;

@Entity
@Table(name = "coordenadores")
public class Coordenador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String area;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
}