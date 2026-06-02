package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestaoService {

    private final QuestaoRepository repository;
    private final DisciplinaService disciplinaService;
    private final SerieService serieService;

    public QuestaoService(QuestaoRepository repository, 
                          DisciplinaService disciplinaService,
                          SerieService serieService) {
        this.repository = repository;
        this.disciplinaService = disciplinaService;
        this.serieService = serieService;
    }

    public List<Questao> listarTodos() {
        return repository.findAll();
    }

    public Optional<Questao> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public Questao salvar(Questao questao) {
        if (questao.getDisciplina() != null && questao.getDisciplina().getId() != null) {
            disciplinaService.buscarPorId(questao.getDisciplina().getId())
                .ifPresent(questao::setDisciplina);
        }
        if (questao.getSerie() != null && questao.getSerie().getId() != null) {
            serieService.buscarPorId(questao.getSerie().getId())
                .ifPresent(questao::setSerie);
        }
        return repository.save(questao);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public List<Questao> buscarPorDisciplina(Integer disciplinaId) {
        return repository.findByDisciplinaId(disciplinaId);
    }

    public List<Questao> buscarPorSerie(Integer serieId) {
        return repository.findBySerieId(serieId);
    }

    public List<Questao> buscarPorDisciplinaESerie(Integer disciplinaId, Integer serieId) {
        return repository.findByDisciplinaIdAndSerieId(disciplinaId, serieId);
    }
}