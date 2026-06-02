package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlternativaRepository extends JpaRepository<Alternativa, Integer> {
    List<Alternativa> findByQuestaoId(Integer questaoId);
}