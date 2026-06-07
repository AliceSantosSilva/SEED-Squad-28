package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Coordenador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CoordenadorRepository extends JpaRepository<Coordenador, Integer> {
    Optional<Coordenador> findByUsuarioId(Integer usuarioId);
    List<Coordenador> findByEscolaId(Integer escolaId);
}