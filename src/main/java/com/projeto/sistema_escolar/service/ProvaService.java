package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.repository.ProvaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProvaService {

    private final ProvaRepository repository;

    public ProvaService(ProvaRepository repository) {
        this.repository = repository;
    }

    public List<Prova> listarTodas() {
        return repository.findAll();
    }

    public Optional<Prova> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Prova> buscarPorProfessor(Long professorId) {
        return repository.findByProfessorId(professorId);
    }

    public Prova salvar(Prova prova) {
        return repository.save(prova);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}