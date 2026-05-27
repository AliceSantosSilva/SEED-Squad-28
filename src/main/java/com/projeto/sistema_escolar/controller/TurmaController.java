package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.service.EscolaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/turmas")
@CrossOrigin(origins = "*")
public class TurmaController {

    private final TurmaService service;
    private final EscolaService escolaService;

    public TurmaController(TurmaService service, EscolaService escolaService) {
        this.service = service;
        this.escolaService = escolaService;
    }

    @GetMapping
    public List<Turma> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Turma> criar(@RequestBody Turma turma) {
        if (turma.getEscola() != null && turma.getEscola().getId() != null) {
            escolaService.buscarPorId(turma.getEscola().getId())
                .ifPresent(turma::setEscola);
        }
        Turma saved = service.salvar(turma);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turma> atualizar(@PathVariable Long id, @RequestBody Turma turmaAtualizada) {
        return service.buscarPorId(id).map(turma -> {
            turma.setNome(turmaAtualizada.getNome());
            turma.setSerie(turmaAtualizada.getSerie());
            turma.setAno(turmaAtualizada.getAno());
            
            if (turmaAtualizada.getEscola() != null && turmaAtualizada.getEscola().getId() != null) {
                escolaService.buscarPorId(turmaAtualizada.getEscola().getId())
                    .ifPresent(turma::setEscola);
            }
            
            return ResponseEntity.ok(service.salvar(turma));
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