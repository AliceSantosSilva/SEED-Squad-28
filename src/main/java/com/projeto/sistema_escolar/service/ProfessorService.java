package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Professor;
import com.projeto.sistema_escolar.repository.ProfessorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    public List<Professor> listarTodos() {
        return repository.findAll();
    }

    public Optional<Professor> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<Professor> buscarPorUsuarioId(Integer usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Professor salvar(Professor professor) {
        return repository.save(professor);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}