package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.ForumProfessor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumProfessorRepository 
        extends JpaRepository<ForumProfessor, Integer> {

}