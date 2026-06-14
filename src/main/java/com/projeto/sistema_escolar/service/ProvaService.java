package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import com.projeto.sistema_escolar.repository.ProvaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProvaService {

    private final ProvaRepository repository;

    private final  QuestaoRepository questaoRepository;

    public ProvaService(ProvaRepository repository, QuestaoRepository questaoRepository) {
        this.repository = repository;
        this.questaoRepository = questaoRepository;
    }

    public List<Prova> listarTodas() {
        return repository.findAll();
    }

    public Optional<Prova> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public List<Prova> buscarPorProfessor(Integer professorId) {
        return repository.findByProfessorId(professorId);
    }

    public List<Prova> buscarPorTurma(Integer turmaId) {
        return repository.findByTurmaId(turmaId);
    }

    public List<Prova> buscarProvasAtivasPorTurma(Integer turmaId) {
        return repository.findProvasAtivasByTurma(turmaId, LocalDateTime.now());
    }

    public List<Prova> buscarProvasPendentes(Integer turmaId, Integer alunoId) {
        return repository.findProvasPendentes(turmaId, alunoId, LocalDateTime.now());
    }

    public Prova salvar(Prova prova) {
        return repository.save(prova);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public long contarProvasAplicadas() {
        return repository.countProvasAplicadas();
    }

    public long contarProvasAplicadasPorEscola(Integer escolaId) {
        return repository.countProvasAplicadasPorEscola(escolaId);
    }

    public List<Prova> buscarPorEscola(Integer escolaId) {
        return repository.findByEscolaId(escolaId);
    }


    public Prova gerarProvaAutomatica(Prova provaBase, Integer disciplinaId, Integer serieId, Integer quantidadeQuestoes) {
        List<Questao> questoesSorteadas = questaoRepository.sortearQuestoesAleatorias(disciplinaId, serieId, quantidadeQuestoes);
        provaBase.setQuestoes(questoesSorteadas);
        return repository.save(provaBase);
    }
}