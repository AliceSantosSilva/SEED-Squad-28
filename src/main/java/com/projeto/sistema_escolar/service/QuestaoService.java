package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class QuestaoService {

    private final QuestaoRepository repository;

    public QuestaoService(QuestaoRepository repository) {
        this.repository = repository;
    }

    public List<Questao> listarTodos() {
        return repository.findAll();
    }

    public Optional<Questao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Questao salvar(Questao questao) {
        return repository.save(questao);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }

    public List<Questao> buscarPorDisciplina(Long disciplinaId) {
        return repository.findByDisciplinaId(disciplinaId);
    }

    public List<Questao> buscarPorSerie(Long serieId) {
        return repository.findBySerieId(serieId);
    }

    public List<Questao> buscarPorDisciplinaESerie(Long disciplinaId, Long serieId) {
        return repository.findByDisciplinaIdAndSerieId(disciplinaId, serieId);
    }
}