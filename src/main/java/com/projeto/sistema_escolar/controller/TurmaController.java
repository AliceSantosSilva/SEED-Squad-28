package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.model.Serie;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.service.EscolaService;
import com.projeto.sistema_escolar.service.SerieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService service;
    private final EscolaService escolaService;
    private final SerieService serieService;

    public TurmaController(TurmaService service, EscolaService escolaService, SerieService serieService) {
        this.service = service;
        this.escolaService = escolaService;
        this.serieService = serieService;
    }

    @GetMapping
    public List<Turma> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscarPorId(@PathVariable Integer id) {
        Optional<Turma> turma = service.buscarPorId(id);
        return turma.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Turma> criar(@Valid @RequestBody Turma turma) {
        if (turma.getEscola() != null && turma.getEscola().getId() != null) {
            escolaService.buscarPorId(turma.getEscola().getId())
                .ifPresent(turma::setEscola);
        }
        if (turma.getSerie() != null && turma.getSerie().getId() != null) {
            serieService.buscarPorId(turma.getSerie().getId())
                .ifPresent(turma::setSerie);
        }
        Turma saved = service.salvar(turma);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turma> atualizar(@PathVariable Integer id, @Valid @RequestBody Turma turmaAtualizada) {
        return service.buscarPorId(id).map(turma -> {
            turma.setNome(turmaAtualizada.getNome());
            turma.setAno(turmaAtualizada.getAno());
            
            if (turmaAtualizada.getEscola() != null && turmaAtualizada.getEscola().getId() != null) {
                escolaService.buscarPorId(turmaAtualizada.getEscola().getId())
                    .ifPresent(turma::setEscola);
            }
            
            if (turmaAtualizada.getSerie() != null && turmaAtualizada.getSerie().getId() != null) {
                serieService.buscarPorId(turmaAtualizada.getSerie().getId())
                    .ifPresent(turma::setSerie);
            }
            
            return ResponseEntity.ok(service.salvar(turma));
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