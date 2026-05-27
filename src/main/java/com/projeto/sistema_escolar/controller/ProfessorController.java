package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Professor;
import com.projeto.sistema_escolar.service.ProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/professores")
@CrossOrigin(origins = "*")
public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Professor> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Professor criar(@RequestBody Professor professor) {
        return service.salvar(professor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizar(@PathVariable Long id, @RequestBody Professor profAtualizado) {
        return service.buscarPorId(id).map(prof -> {
            prof.setUsuario(profAtualizado.getUsuario());
            prof.setEspecialidade(profAtualizado.getEspecialidade());
            return ResponseEntity.ok(service.salvar(prof));
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