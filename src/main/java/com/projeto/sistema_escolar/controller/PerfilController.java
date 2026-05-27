package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Perfil;
import com.projeto.sistema_escolar.service.PerfilService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/perfis")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Perfil> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Perfil criar(@RequestBody Perfil perfil) {
        return service.salvar(perfil);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizar(@PathVariable Long id, @RequestBody Perfil perfilAtualizado) {
        return service.buscarPorId(id).map(perfil -> {
            perfil.setNome(perfilAtualizado.getNome());
            perfil.setDescricao(perfilAtualizado.getDescricao());
            return ResponseEntity.ok(service.salvar(perfil));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.existePorId(id)) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}