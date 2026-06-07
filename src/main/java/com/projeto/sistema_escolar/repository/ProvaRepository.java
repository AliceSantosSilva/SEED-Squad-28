package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ProvaRepository extends JpaRepository<Prova, Integer> {

    List<Prova> findByProfessorId(Integer professorId);

    List<Prova> findByTurmaId(Integer turmaId);

    // Provas ativas da turma (dentro do período)
    @Query("""
        SELECT p FROM Prova p
        WHERE p.turma.id = :turmaId
        AND p.ativo = true
        AND p.dataInicio <= :agora
        AND p.dataFim >= :agora
    """)
    List<Prova> findProvasAtivasByTurma(
        @Param("turmaId") Integer turmaId,
        @Param("agora") LocalDateTime agora
    );

    // Provas que o aluno ainda não respondeu
    @Query("""
        SELECT p FROM Prova p
        WHERE p.turma.id = :turmaId
        AND p.ativo = true
        AND p.dataFim >= :agora
        AND p.id NOT IN (
            SELECT DISTINCT r.prova.id FROM Resposta r
            WHERE r.aluno.id = :alunoId
        )
    """)
    List<Prova> findProvasPendentes(
        @Param("turmaId") Integer turmaId,
        @Param("alunoId") Integer alunoId,
        @Param("agora") LocalDateTime agora
    );

    // Total de provas que já têm pelo menos uma resposta
    @Query("""
        SELECT COUNT(DISTINCT r.prova.id) FROM Resposta r
    """)
    Long countProvasAplicadas();

    // Total de provas aplicadas em uma escola específica
    @Query("""
        SELECT COUNT(DISTINCT r.prova.id) FROM Resposta r
        WHERE r.prova.turma.escola.id = :escolaId
    """)
    Long countProvasAplicadasPorEscola(@Param("escolaId") Integer escolaId);

    // Todas as provas de uma escola
    @Query("""
        SELECT p FROM Prova p
        WHERE p.turma.escola.id = :escolaId
    """)
    List<Prova> findByEscolaId(@Param("escolaId") Integer escolaId);
}