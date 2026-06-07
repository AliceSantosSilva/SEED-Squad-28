package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Integer> {

    // ← NOVO: necessário para o CadastroPublicoController
    Optional<Perfil> findByNome(String nome);
}