package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    List<Questao> findByDisciplinaId(Long disciplinaId);
    List<Questao> findBySerieId(Long serieId);
    List<Questao> findByDisciplinaIdAndSerieId(Long disciplinaId, Long serieId);
}