package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.SeedTable;
import com.projeto.sistema_escolar.repository.SeedTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SeedTableService {

    private final SeedTableRepository repository;

    public SeedTableService(SeedTableRepository repository) {
        this.repository = repository;
    }

    public List<SeedTable> listarTodos() {
        return repository.findAll();
    }

    public Optional<SeedTable> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<SeedTable> buscarPorNome(String nome) {
        return repository.findByNome(nome);
    }

    public SeedTable salvar(SeedTable seed) {
        return repository.save(seed);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}