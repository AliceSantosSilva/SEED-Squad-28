package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Integer> {
    Optional<Professor> findByUsuarioId(Integer usuarioId);
    List<Professor> findByEscolaId(Integer escolaId);
}