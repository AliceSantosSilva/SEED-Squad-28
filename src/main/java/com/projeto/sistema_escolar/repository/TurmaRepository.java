package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TurmaRepository extends JpaRepository<Turma, Integer> {

    List<Turma> findByEscolaId(Integer escolaId);

    // Turmas onde o professor tem provas cadastradas
    @Query("""
        SELECT DISTINCT p.turma FROM Prova p
        WHERE p.professor.id = :professorId
        AND p.turma IS NOT NULL
    """)
    List<Turma> findTurmasByProfessorId(@Param("professorId") Integer professorId);
}