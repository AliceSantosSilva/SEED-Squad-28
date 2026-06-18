package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Integer> {
    List<Questao> findByDisciplinaId(Integer disciplinaId);
    List<Questao> findBySerieId(Integer serieId);
    List<Questao> findByDisciplinaIdAndSerieId(Integer disciplinaId, Integer serieId);
    @Query(value = "SELECT * FROM questoes WHERE disciplina_id = :disciplinaId AND serie_id = :serieId ORDER BY RANDOM() LIMIT :quantidade", nativeQuery = true)
    List<Questao> sortearQuestoesAleatorias(
            @Param("disciplinaId") Integer disciplinaId,
            @Param("serieId") Integer serieId,
            @Param("quantidade") Integer quantidade
    );
}