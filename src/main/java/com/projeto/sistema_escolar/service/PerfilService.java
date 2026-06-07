package com.projeto.sistema_escolar.service;

import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PerfilService {

    private final PerfilRepository repository;

    public PerfilService(PerfilRepository repository) {
        this.repository = repository;
    }

    public List<Perfil> listarTodos() {
        return repository.findAll();
    }

    public Optional<Perfil> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    // ← NOVO: busca por nome para o CadastroPublicoController
    public Optional<Perfil> buscarPorNome(String nome) {
        return repository.findByNome(nome);
    }

    public Perfil salvar(Perfil perfil) {
        return repository.save(perfil);
    }

    public void deletar(Integer id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Integer id) {
        return repository.existsById(id);
    }
}