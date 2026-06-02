package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Integer> {
}