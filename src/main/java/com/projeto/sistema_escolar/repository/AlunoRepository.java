package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Integer> {
    Optional<Aluno> findByUsuarioId(Integer usuarioId);
}