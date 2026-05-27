package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.SeedTable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SeedTableRepository extends JpaRepository<SeedTable, Long> {
    Optional<SeedTable> findByNome(String nome);
}