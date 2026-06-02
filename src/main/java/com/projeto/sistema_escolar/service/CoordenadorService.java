package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Coordenador;
import com.projeto.sistema_escolar.repository.CoordenadorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CoordenadorService {

    private final CoordenadorRepository repository;

    public CoordenadorService(CoordenadorRepository repository) {
        this.repository = repository;
    }

    public List<Coordenador> listarTodos() {
        return repository.findAll();
    }

    public Optional<Coordenador> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<Coordenador> buscarPorUsuarioId(Integer usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Coordenador salvar(Coordenador coordenador) {
        return repository.save(coordenador);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}