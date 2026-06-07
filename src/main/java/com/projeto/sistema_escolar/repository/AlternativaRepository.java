package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Alternativa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlternativaRepository extends JpaRepository<Alternativa, Integer> {

    List<Alternativa> findByQuestaoId(Integer questaoId);

    // Busca a alternativa correta de uma questão
    Optional<Alternativa> findByQuestaoIdAndCorretaTrue(Integer questaoId);
}