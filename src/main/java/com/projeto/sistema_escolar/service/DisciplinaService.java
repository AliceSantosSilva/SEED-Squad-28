package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Disciplina;
import com.projeto.sistema_escolar.repository.DisciplinaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DisciplinaService {

    private final DisciplinaRepository repository;

    public DisciplinaService(DisciplinaRepository repository) {
        this.repository = repository;
    }

    public List<Disciplina> listarTodos() {
        return repository.findAll();
    }

    public Optional<Disciplina> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Disciplina salvar(Disciplina disciplina) {
        return repository.save(disciplina);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}