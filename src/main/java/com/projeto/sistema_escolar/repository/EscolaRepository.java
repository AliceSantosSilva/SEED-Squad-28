package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Escola;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscolaRepository extends JpaRepository<Escola, Integer> {
}