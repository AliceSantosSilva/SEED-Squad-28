package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, Long> {
}