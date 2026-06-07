package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Resposta;
import com.projeto.sistema_escolar.repository.RespostaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RespostaService {

    private final RespostaRepository repository;

    public RespostaService(RespostaRepository repository) {
        this.repository = repository;
    }

    public List<Resposta> listarTodas() {
        return repository.findAll();
    }

    public Optional<Resposta> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public List<Resposta> buscarPorAluno(Integer alunoId) {
        return repository.findByAlunoId(alunoId);
    }

    public List<Resposta> buscarPorProva(Integer provaId) {
        return repository.findByProvaId(provaId);
    }

    public List<Resposta> buscarPorAlunoEProva(Integer alunoId, Integer provaId) {
        return repository.findByAlunoIdAndProvaId(alunoId, provaId);
    }

    public boolean jaRespondeu(Integer alunoId, Integer provaId) {
        return repository.existsByAlunoIdAndProvaId(alunoId, provaId);
    }

    public List<Integer> buscarIdsProvasRespondidas(Integer alunoId) {
        return repository.findProvasRespondidasIdsByAluno(alunoId);
    }

    public long contarAcertos(Integer alunoId, Integer provaId) {
        return repository.countRespostasCorretas(alunoId, provaId);
    }

    public long contarTotalRespostas(Integer alunoId, Integer provaId) {
        return repository.countRespostasTotal(alunoId, provaId);
    }

    public Resposta salvar(Resposta resposta) {
        return repository.save(resposta);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }

    public long contarAlunosQueResponderam(Integer provaId) {
        return repository.countAlunosQueResponderamProva(provaId);
    }

    public double calcularMediaProva(Integer provaId) {
        Double media = repository.calcularMediaProva(provaId);
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }

    public double calcularMediaGeral() {
        Double media = repository.calcularMediaGeral();
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }

    public double calcularMediaGeralPorEscola(Integer escolaId) {
        Double media = repository.calcularMediaGeralPorEscola(escolaId);
        if (media == null) return 0.0;
        return Math.round(media * 100.0) / 100.0;
    }
}