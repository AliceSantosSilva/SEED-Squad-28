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

    public Optional<Perfil> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Perfil salvar(Perfil perfil) {
        return repository.save(perfil);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    public boolean existePorId(Long id) {
        return repository.existsById(id);
    }
}