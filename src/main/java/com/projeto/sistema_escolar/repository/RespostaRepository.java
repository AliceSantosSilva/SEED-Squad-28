package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.projeto.sistema_escolar.dto.DesempenhoDisciplinaDTO;

public interface RespostaRepository extends JpaRepository<Resposta, Integer> {

    List<Resposta> findByAlunoId(Integer alunoId);

    List<Resposta> findByProvaId(Integer provaId);

    List<Resposta> findByAlunoIdAndProvaId(Integer alunoId, Integer provaId);

    boolean existsByAlunoIdAndProvaId(Integer alunoId, Integer provaId);

    // IDs de provas já respondidas pelo aluno
    @Query("""
        SELECT DISTINCT r.prova.id FROM Resposta r
        WHERE r.aluno.id = :alunoId
    """)
    List<Integer> findProvasRespondidasIdsByAluno(@Param("alunoId") Integer alunoId);

    // Total de respostas corretas do aluno em uma prova
    @Query("""
        SELECT COUNT(r) FROM Resposta r
        WHERE r.aluno.id = :alunoId
        AND r.prova.id = :provaId
        AND r.correta = true
    """)
    Long countRespostasCorretas(
        @Param("alunoId") Integer alunoId,
        @Param("provaId") Integer provaId
    );

    // Total de respostas do aluno em uma prova
    @Query("""
        SELECT COUNT(r) FROM Resposta r
        WHERE r.aluno.id = :alunoId
        AND r.prova.id = :provaId
    """)
    Long countRespostasTotal(
        @Param("alunoId") Integer alunoId,
        @Param("provaId") Integer provaId
    );

    // Total de alunos distintos que responderam uma prova
    @Query("""
        SELECT COUNT(DISTINCT r.aluno.id) FROM Resposta r
        WHERE r.prova.id = :provaId
    """)
    Long countAlunosQueResponderamProva(@Param("provaId") Integer provaId);

    // Média geral de acertos de uma prova (0 a 10)
    @Query("""
        SELECT AVG(
            (SELECT COUNT(r2) FROM Resposta r2
             WHERE r2.aluno.id = r.aluno.id
             AND r2.prova.id = :provaId
             AND r2.correta = true) * 10.0 /
            NULLIF((SELECT COUNT(r3) FROM Resposta r3
             WHERE r3.aluno.id = r.aluno.id
             AND r3.prova.id = :provaId), 0)
        )
        FROM Resposta r
        WHERE r.prova.id = :provaId
    """)
    Double calcularMediaProva(@Param("provaId") Integer provaId);

    // Média geral de todas as provas do sistema
    @Query("""
        SELECT AVG(
            (SELECT COUNT(r2) FROM Resposta r2
             WHERE r2.aluno.id = r.aluno.id
             AND r2.prova.id = r.prova.id
             AND r2.correta = true) * 10.0 /
            NULLIF((SELECT COUNT(r3) FROM Resposta r3
             WHERE r3.aluno.id = r.aluno.id
             AND r3.prova.id = r.prova.id), 0)
        )
        FROM Resposta r
    """)
    Double calcularMediaGeral();

    // Média geral por escola
    @Query("""
        SELECT AVG(
            (SELECT COUNT(r2) FROM Resposta r2
             WHERE r2.aluno.id = r.aluno.id
             AND r2.prova.id = r.prova.id
             AND r2.correta = true) * 10.0 /
            NULLIF((SELECT COUNT(r3) FROM Resposta r3
             WHERE r3.aluno.id = r.aluno.id
             AND r3.prova.id = r.prova.id), 0)
        )
        FROM Resposta r
        WHERE r.prova.turma.escola.id = :escolaId
    """)
    Double calcularMediaGeralPorEscola(@Param("escolaId") Integer escolaId);

    @Query("""
        SELECT new com.projeto.sistema_escolar.dto.DesempenhoDisciplinaDTO(
            d.id, d.nome, COUNT(r), SUM(CASE WHEN r.correta = true THEN 1 ELSE 0 END)
        )
        FROM Resposta r
        JOIN r.questao q
        JOIN q.disciplina d
        WHERE r.aluno.id = :alunoId
        GROUP BY d.id, d.nome
    """)
    List<DesempenhoDisciplinaDTO> calcularDesempenhoPorDisciplina(@Param("alunoId") Integer alunoId);
}