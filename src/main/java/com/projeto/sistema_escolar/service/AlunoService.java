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

    public Optional<Aluno> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Optional<Aluno> buscarPorUsuarioId(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId);
    }

    public Aluno salvar(Aluno aluno) {
        return repository.save(aluno);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}