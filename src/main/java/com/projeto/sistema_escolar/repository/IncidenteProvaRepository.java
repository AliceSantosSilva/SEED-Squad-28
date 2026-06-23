package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.IncidenteProva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidenteProvaRepository extends JpaRepository<IncidenteProva, Integer> {

    /**
     * Busca todos os incidentes de um aluno em uma prova específica
     */
    List<IncidenteProva> findByAlunoIdAndProvaId(Integer alunoId, Integer provaId);

    /**
     * Conta quantos incidentes um aluno teve em uma prova específica
     */
    long countByAlunoIdAndProvaId(Integer alunoId, Integer provaId);

    /**
     * Busca todos os incidentes de um aluno (todas as provas)
     */
    List<IncidenteProva> findByAlunoId(Integer alunoId);

    /**
     * Busca todos os incidentes de uma prova (todos os alunos)
     */
    List<IncidenteProva> findByProvaId(Integer provaId);

    /**
     * Busca incidentes por tipo (ex: "SAIU_DA_ABA")
     */
    List<IncidenteProva> findByTipo(String tipo);

    /**
     * Busca incidentes de um aluno filtrando por tipo
     */
    List<IncidenteProva> findByAlunoIdAndTipo(Integer alunoId, String tipo);

    /**
     * Conta quantos incidentes de um tipo específico um aluno teve em uma prova
     */
    @Query("SELECT COUNT(i) FROM IncidenteProva i WHERE i.aluno.id = :alunoId AND i.prova.id = :provaId AND i.tipo = :tipo")
    long countByAlunoIdAndProvaIdAndTipo(@Param("alunoId") Integer alunoId,
                                         @Param("provaId") Integer provaId,
                                         @Param("tipo") String tipo);
}