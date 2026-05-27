package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
}