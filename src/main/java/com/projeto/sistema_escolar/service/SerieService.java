package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Serie;
import com.projeto.sistema_escolar.repository.SerieRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SerieService {

    private final SerieRepository repository;

    public SerieService(SerieRepository repository) {
        this.repository = repository;
    }

    public List<Serie> listarTodos() {
        return repository.findAll();
    }

    public Optional<Serie> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Serie salvar(Serie serie) {
        return repository.save(serie);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}