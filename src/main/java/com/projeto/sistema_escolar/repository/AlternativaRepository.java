package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlternativaRepository extends JpaRepository<Alternativa, Long> {
    List<Alternativa> findByQuestaoId(Long questaoId);
}