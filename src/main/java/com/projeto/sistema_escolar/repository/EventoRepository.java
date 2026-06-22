package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
    List<Evento> findByTipo(String tipo);
}