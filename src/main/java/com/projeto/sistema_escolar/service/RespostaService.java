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

    public Resposta salvar(Resposta resposta) {
        return repository.save(resposta);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}