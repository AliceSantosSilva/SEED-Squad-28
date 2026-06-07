package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Alternativa;
import com.projeto.sistema_escolar.repository.AlternativaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlternativaService {

    private final AlternativaRepository repository;

    public AlternativaService(AlternativaRepository repository) {
        this.repository = repository;
    }

    public List<Alternativa> listarTodas() {
        return repository.findAll();
    }

    public Optional<Alternativa> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public List<Alternativa> buscarPorQuestao(Integer questaoId) {
        return repository.findByQuestaoId(questaoId);
    }

    public Alternativa salvar(Alternativa alternativa) {
        return repository.save(alternativa);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public Optional<Alternativa> buscarCorretaPorQuestao(Integer questaoId) {
        return repository.findByQuestaoIdAndCorretaTrue(questaoId);
    }
}