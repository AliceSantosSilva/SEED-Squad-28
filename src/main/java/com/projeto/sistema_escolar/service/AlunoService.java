package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Aluno;
import com.projeto.sistema_escolar.repository.AlunoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final AlunoRepository repository;

    public AlunoService(AlunoRepository repository) {
        this.repository = repository;
    }

    public List<Aluno> listarTodos() {
        return repository.findAll();
    }

    public Optional<Aluno> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Optional<Aluno> buscarPorUsuarioId(Integer usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Aluno salvar(Aluno aluno) {
        return repository.save(aluno);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}