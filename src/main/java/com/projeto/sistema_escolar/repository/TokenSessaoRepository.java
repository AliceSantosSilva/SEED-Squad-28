package com.projeto.sistema_escolar.repository;

import com.projeto.sistema_escolar.model.TokenSessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TokenSessaoRepository extends JpaRepository<TokenSessao, Long> {
    Optional<TokenSessao> findByToken(String token);
    List<TokenSessao> findByUsuarioId(Long usuarioId);
}