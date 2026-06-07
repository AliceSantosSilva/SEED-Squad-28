package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Professor;
import com.projeto.sistema_escolar.model.Escola;
import com.projeto.sistema_escolar.service.ProfessorService;
import com.projeto.sistema_escolar.service.EscolaService;
import com.projeto.sistema_escolar.util.EscolaFiltroUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/professores")
public class ProfessorController {

    private final ProfessorService service;
    private final EscolaService escolaService;
    private final EscolaFiltroUtil filtroUtil;

    public ProfessorController(ProfessorService service, EscolaService escolaService, EscolaFiltroUtil filtroUtil) {
        this.service = service;
        this.escolaService = escolaService;
        this.filtroUtil = filtroUtil;
    }

    @GetMapping
    public List<Professor> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return service.listarPorEscola(escolaId);
        }
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarPorId(@PathVariable Integer id) {
        Optional<Professor> professor = service.buscarPorId(id);
        return professor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Professor> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return service.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Professor> criar(@Valid @RequestBody Professor professor) {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            Escola escola = escolaService.buscarPorId(escolaId)
                    .orElseThrow(() -> new RuntimeException("Escola não encontrada"));
            professor.setEscola(escola);
        }
        Professor saved = service.salvar(professor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> atualizar(@PathVariable Integer id, @Valid @RequestBody Professor professorAtualizado) {
        Optional<Professor> professorOpt = service.buscarPorId(id);
        if (professorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Professor professor = professorOpt.get();
        professor.setDisciplina(professorAtualizado.getDisciplina());
        professor.setEspecialidade(professorAtualizado.getEspecialidade());
        
        if (professorAtualizado.getEscola() != null && professorAtualizado.getEscola().getId() != null) {
            Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
            if (escolaId != null && !professorAtualizado.getEscola().getId().equals(escolaId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            professor.setEscola(professorAtualizado.getEscola());
        }
        professor.setUsuario(professorAtualizado.getUsuario());
        
        Professor saved = service.salvar(professor);
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