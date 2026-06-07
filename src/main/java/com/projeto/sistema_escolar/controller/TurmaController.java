package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.service.EscolaService;
import com.projeto.sistema_escolar.service.SerieService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.util.EscolaFiltroUtil;
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
    private final EscolaFiltroUtil filtroUtil;

    public TurmaController(TurmaService service, EscolaService escolaService, SerieService serieService, EscolaFiltroUtil filtroUtil) {
        this.service = service;
        this.escolaService = escolaService;
        this.serieService = serieService;
        this.filtroUtil = filtroUtil;
    }

    @GetMapping
    public List<Turma> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return service.listarPorEscola(escolaId);
        }
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turma> buscarPorId(@PathVariable Integer id) {
        Optional<Turma> turma = service.buscarPorId(id);
        return turma.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Turma> criar(@Valid @RequestBody Turma turma) {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            Escola escola = escolaService.buscarPorId(escolaId)
                    .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            turma.setEscola(escola);
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
        Optional<Turma> turmaOpt = service.buscarPorId(id);
        if (turmaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Turma turma = turmaOpt.get();
        turma.setNome(turmaAtualizada.getNome());
        turma.setAno(turmaAtualizada.getAno());
        
        if (turmaAtualizada.getSerie() != null && turmaAtualizada.getSerie().getId() != null) {
            serieService.buscarPorId(turmaAtualizada.getSerie().getId())
                .ifPresent(turma::setSerie);
        }
        
        if (turmaAtualizada.getEscola() != null && turmaAtualizada.getEscola().getId() != null) {
            Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
            if (escolaId != null && !turmaAtualizada.getEscola().getId().equals(escolaId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            turma.setEscola(turmaAtualizada.getEscola());
        }
        
        Turma saved = service.salvar(turma);
        return ResponseEntity.ok(saved);
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