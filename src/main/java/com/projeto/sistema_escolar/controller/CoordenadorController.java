package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Coordenador;
import com.projeto.sistema_escolar.model.Professor;
import com.projeto.sistema_escolar.model.Turma;
import com.projeto.sistema_escolar.service.CoordenadorService;
import com.projeto.sistema_escolar.service.EscolaService;
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
@RequestMapping("/api/coordenador")
public class CoordenadorController {

    private final CoordenadorService coordenadorService;
    private final EscolaService escolaService;
    private final ProfessorService professorService;
    private final TurmaService turmaService;
    private final EscolaFiltroUtil filtroUtil;

    public CoordenadorController(CoordenadorService coordenadorService,
                                 EscolaService escolaService,
                                 ProfessorService professorService,
                                 TurmaService turmaService,
                                 EscolaFiltroUtil filtroUtil) {
        this.coordenadorService = coordenadorService;
        this.escolaService = escolaService;
        this.professorService = professorService;
        this.turmaService = turmaService;
        this.filtroUtil = filtroUtil;
    }

    // ==================== ENDPOINTS PARA O FRONTEND ====================

    @GetMapping("/minha-escola")
    public ResponseEntity<?> getMinhaEscola() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId == null) {
            return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
        }
        
        return escolaService.buscarPorId(escolaId)
                .map(escola -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", escola.getId());
                    response.put("nome", escola.getNome());
                    response.put("cidade", escola.getCidade() != null ? escola.getCidade() : "—");
                    response.put("totalAlunos", 0);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/meus-professores")
    public ResponseEntity<List<Map<String, Object>>> getMeusProfessores() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId == null) {
            return ResponseEntity.status(403).build();
        }

        List<Professor> professores = professorService.listarPorEscola(escolaId);
        
        List<Map<String, Object>> response = professores.stream().map(prof -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", prof.getId());
            item.put("nome", prof.getUsuario() != null ? prof.getUsuario().getNome() : "—");
            item.put("disciplina", prof.getDisciplina() != null ? prof.getDisciplina() : "—");
            item.put("escola", prof.getEscola() != null ? prof.getEscola().getNome() : "—");
            item.put("status", "Ativo");
            item.put("totalTurmas", 0);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

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
            item.put("totalAlunos", 0);
            item.put("mediaGeral", "—");
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ==================== ENDPOINTS ORIGINAIS (CRUD) ====================

    @GetMapping("/listar")
    public List<Coordenador> listar() {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return coordenadorService.listarPorEscola(escolaId);
        }
        return coordenadorService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coordenador> buscarPorId(@PathVariable Integer id) {
        Optional<Coordenador> coordenador = coordenadorService.buscarPorId(id);
        return coordenador.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Coordenador> buscarPorUsuario(@PathVariable Integer usuarioId) {
        return coordenadorService.buscarPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/criar")
    public ResponseEntity<Coordenador> criar(@Valid @RequestBody Coordenador coordenador) {
        Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
        if (escolaId != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (coordenador.getEscola() != null && coordenador.getEscola().getId() != null) {
            escolaService.buscarPorId(coordenador.getEscola().getId())
                .ifPresent(coordenador::setEscola);
        }
        Coordenador saved = coordenadorService.salvar(coordenador);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<Coordenador> atualizar(@PathVariable Integer id, @Valid @RequestBody Coordenador coordAtualizado) {
        Optional<Coordenador> coordOpt = coordenadorService.buscarPorId(id);
        if (coordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Coordenador coord = coordOpt.get();
        coord.setArea(coordAtualizado.getArea());
        
        if (coordAtualizado.getEscola() != null && coordAtualizado.getEscola().getId() != null) {
            Integer escolaId = filtroUtil.getEscolaIdDoUsuarioLogado();
            if (escolaId != null && !coordAtualizado.getEscola().getId().equals(escolaId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            coord.setEscola(coordAtualizado.getEscola());
        }
        coord.setUsuario(coordAtualizado.getUsuario());
        
        Coordenador saved = coordenadorService.salvar(coord);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (coordenadorService.existePorId(id)) {
            coordenadorService.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}