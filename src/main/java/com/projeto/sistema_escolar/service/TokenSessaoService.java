package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.TokenSessao;
import com.projeto.sistema_escolar.repository.TokenSessaoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TokenSessaoService {

    private final TokenSessaoRepository repository;

    public TokenSessaoService(TokenSessaoRepository repository) {
        this.repository = repository;
    }

    public List<TokenSessao> listarTodos() {
        return repository.findAll();
    }

    public Optional<TokenSessao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<TokenSessao> buscarPorToken(String token) {
        return repository.findByToken(token);
    }

    public List<TokenSessao> buscarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public TokenSessao salvar(TokenSessao token) {
        return repository.save(token);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}