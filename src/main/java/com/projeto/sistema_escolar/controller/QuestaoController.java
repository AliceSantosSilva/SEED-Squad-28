package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Disciplina;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.model.Serie;
import com.projeto.sistema_escolar.service.DisciplinaService;
import com.projeto.sistema_escolar.service.QuestaoService;
import com.projeto.sistema_escolar.service.SerieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "*")
public class QuestaoController {

    private final QuestaoService service;
    private final DisciplinaService disciplinaService;
    private final SerieService serieService;

    public QuestaoController(QuestaoService service,
                             DisciplinaService disciplinaService,
                             SerieService serieService) {
        this.service = service;
        this.disciplinaService = disciplinaService;
        this.serieService = serieService;
    }

    @GetMapping
    public List<Questao> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questao> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disciplina/{disciplinaId}")
    public List<Questao> buscarPorDisciplina(@PathVariable Long disciplinaId) {
        return service.buscarPorDisciplina(disciplinaId);
    }

    @GetMapping("/serie/{serieId}")
    public List<Questao> buscarPorSerie(@PathVariable Long serieId) {
        return service.buscarPorSerie(serieId);
    }

    @GetMapping("/filtro")
    public List<Questao> buscarPorDisciplinaESerie(
            @RequestParam Long disciplinaId,
            @RequestParam Long serieId) {
        return service.buscarPorDisciplinaESerie(disciplinaId, serieId);
    }

    @PostMapping
    public ResponseEntity<Questao> criar(@RequestBody Questao questao) {
        // Buscar disciplina e serie completas do banco antes de salvar
        if (questao.getDisciplina() != null && questao.getDisciplina().getId() != null) {
            disciplinaService.buscarPorId(questao.getDisciplina().getId())
                .ifPresent(questao::setDisciplina);
        }
        if (questao.getSerie() != null && questao.getSerie().getId() != null) {
            serieService.buscarPorId(questao.getSerie().getId())
                .ifPresent(questao::setSerie);
        }
        
        Questao saved = service.salvar(questao);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questao> atualizar(@PathVariable Long id, @RequestBody Questao questaoAtualizada) {
        return service.buscarPorId(id).map(questao -> {
            questao.setEnunciado(questaoAtualizada.getEnunciado());
            questao.setDificuldade(questaoAtualizada.getDificuldade());
            
            // Garantir disciplina completa
            if (questaoAtualizada.getDisciplina() != null && questaoAtualizada.getDisciplina().getId() != null) {
                disciplinaService.buscarPorId(questaoAtualizada.getDisciplina().getId())
                    .ifPresent(questao::setDisciplina);
            }
            
            // Garantir serie completa
            if (questaoAtualizada.getSerie() != null && questaoAtualizada.getSerie().getId() != null) {
                serieService.buscarPorId(questaoAtualizada.getSerie().getId())
                    .ifPresent(questao::setSerie);
            }
            
            return ResponseEntity.ok(service.salvar(questao));
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