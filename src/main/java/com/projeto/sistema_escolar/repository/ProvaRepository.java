package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Prova;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProvaRepository extends JpaRepository<Prova, Long> {
    List<Prova> findByProfessorId(Long professorId);
}