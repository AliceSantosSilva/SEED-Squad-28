package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Professor;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.service.ProfessorService;
import com.projeto.sistema_escolar.service.TurmaService;
import com.projeto.sistema_escolar.util.EscolaFiltroUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professor")
public class ProfessorController {

    private final ProfessorService professorService;
    private final TurmaService turmaService;
    private final EscolaFiltroUtil filtroUtil;

    public ProfessorController(ProfessorService professorService,
                               TurmaService turmaService,
                               EscolaFiltroUtil filtroUtil) {
        this.professorService = professorService;
        this.turmaService = turmaService;
        this.filtroUtil = filtroUtil;
    }

    // ==================== ENDPOINTS PARA O FRONTEND (COM NOMES ÚNICOS) ====================

    @GetMapping("/minhas-turmas")
    public ResponseEntity<List<Map<String, Object>>> getMinhasTurmas() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId == null) {
            return ResponseEntity.status(403).build();
        }

        List<Turma> turmas = turmaService.listarPorEscola(escolaId);
        
        List<Map<String, Object>> response = turmas.stream().map(turma -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", turma.getId());
            item.put("nome", turma.getNome() != null ? turma.getNome() : "—");
            item.put("serie", turma.getSerie() != null ? turma.getSerie().getNome() : "—");
            item.put("alunos", 0);
            item.put("media", "—");
            item.put("ultimaProva", "—");
            item.put("disciplina", "Matemática");
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/minhas-provas/recentes")
    public ResponseEntity<List<Map<String, Object>>> getMinhasProvasRecentes() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/minhas-provas")
    public ResponseEntity<List<Map<String, Object>>> getMinhasProvas() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/meus-resultados")
    public ResponseEntity<List<Map<String, Object>>> getMeusResultados() {
        return ResponseEntity.ok(List.of());
    }

    // ==================== ENDPOINTS ORIGINAIS (CRUD) ====================

    @GetMapping("/listar")
    public List<Professor> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return professorService.listarPorEscola(escolaId);
        }
        return professorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> buscarPorId(@PathVariable Integer id) {
        Optional<Professor> professor = professorService.buscarPorId(id);
        return professor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Professor> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return professorService.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/criar")
    public ResponseEntity<Professor> criar(@Valid @RequestBody Professor professor) {
        Professor saved = professorService.salvar(professor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Professor> atualizar(@PathVariable Integer id, @Valid @RequestBody Professor professorAtualizado) {
        Optional<Professor> professorOpt = professorService.buscarPorId(id);
        if (professorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Professor professor = professorOpt.get();
        professor.setDisciplina(professorAtualizado.getDisciplina());
        professor.setEspecialidade(professorAtualizado.getEspecialidade());
        professor.setUsuario(professorAtualizado.getUsuario());
        
        Professor saved = professorService.salvar(professor);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (professorService.existePorId(id)) {
            professorService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}