package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    private final TurmaRepository repository;

    public TurmaService(TurmaRepository repository) {
        this.repository = repository;
    }

    public List<Turma> listarTodos() {
        return repository.findAll();
    }

    public Optional<Turma> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Turma salvar(Turma turma) {
        return repository.save(turma);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}