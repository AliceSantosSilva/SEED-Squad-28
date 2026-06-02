package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.repository.EscolaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EscolaService {

    private final EscolaRepository repository;

    public EscolaService(EscolaRepository repository) {
        this.repository = repository;
    }

    public List<Escola> listarTodos() {
        return repository.findAll();
    }

    public Optional<Escola> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Escola salvar(Escola escola) {
        return repository.save(escola);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}