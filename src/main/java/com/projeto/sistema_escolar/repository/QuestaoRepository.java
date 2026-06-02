package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Integer> {
    List<Questao> findByDisciplinaId(Integer disciplinaId);
    List<Questao> findBySerieId(Integer serieId);
    List<Questao> findByDisciplinaIdAndSerieId(Integer disciplinaId, Integer serieId);
}