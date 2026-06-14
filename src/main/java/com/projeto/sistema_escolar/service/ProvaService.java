package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.repository.ProvaRepository;
import com.projeto.sistema_escolar.repository.QuestaoRepository;
import com.projeto.sistema_escolar.repository.TurmaRepository;
import com.projeto.sistema_escolar.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.projeto.sistema_escolar.model.Questao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProvaService {

    private final ProvaRepository   repository;
    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository   turmaRepository;
    private final QuestaoRepository questaoRepository;

    public ProvaService(ProvaRepository repository,
                        UsuarioRepository usuarioRepository,
                        TurmaRepository turmaRepository,
                        QuestaoRepository questaoRepository) {
        this.repository          = repository;
        this.usuarioRepository   = usuarioRepository;
        this.turmaRepository     = turmaRepository;
        this.questaoRepository   = questaoRepository;
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
        if (prova.getProfessor() != null && prova.getProfessor().getId() != null) {
            usuarioRepository.findById(prova.getProfessor().getId())
                .ifPresent(prova::setProfessor);
        }
        if (prova.getTurma() != null && prova.getTurma().getId() != null) {
            turmaRepository.findById(prova.getTurma().getId())
                .ifPresent(prova::setTurma);
        }
        if (prova.getQuestoes() != null) {
            List<Questao> questoes = prova.getQuestoes().stream()
                .map((Questao q) -> questaoRepository.findById(q.getId()).orElse(null))
                .filter(q -> q != null)
                .toList();
            prova.setQuestoes(questoes);
        }
        Prova salva = repository.save(prova);
        return repository.findById(salva.getId()).orElse(salva);
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
}