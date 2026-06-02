package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfis")
public class PerfilController {

    private final PerfilService service;

    public PerfilController(PerfilService service) {
        this.service = service;
    }

    @GetMapping
    public List<Perfil> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Perfil> buscarPorId(@PathVariable Integer id) {
        Optional<Perfil> perfil = service.buscarPorId(id);
        return perfil.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Perfil> criar(@Valid @RequestBody Perfil perfil) {
        Perfil saved = service.salvar(perfil);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizar(@PathVariable Integer id, @Valid @RequestBody Perfil perfilAtualizado) {
        return service.buscarPorId(id).map(perfil -> {
            perfil.setNome(perfilAtualizado.getNome());
            perfil.setDescricao(perfilAtualizado.getDescricao());
            return ResponseEntity.ok(service.salvar(perfil));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}