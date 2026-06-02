package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Resposta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RespostaRepository extends JpaRepository<Resposta, Integer> {
    List<Resposta> findByAlunoId(Integer alunoId);
    List<Resposta> findByProvaId(Integer provaId);
}