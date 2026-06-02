package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.service.QuestaoService;
import com.projeto.sistema_escolar.service.DisciplinaService;
import com.projeto.sistema_escolar.service.SerieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questoes")
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
    public ResponseEntity<Questao> buscarPorId(@PathVariable Integer id) {
        Optional<Questao> questao = service.buscarPorId(id);
        return questao.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/disciplina/{disciplinaId}")
    public List<Questao> buscarPorDisciplina(@PathVariable Integer disciplinaId) {
        return service.buscarPorDisciplina(disciplinaId);
    }

    @GetMapping("/serie/{serieId}")
    public List<Questao> buscarPorSerie(@PathVariable Integer serieId) {
        return service.buscarPorSerie(serieId);
    }

    @GetMapping("/filtro")
    public List<Questao> buscarPorDisciplinaESerie(
            @RequestParam Integer disciplinaId,
            @RequestParam Integer serieId) {
        return service.buscarPorDisciplinaESerie(disciplinaId, serieId);
    }

    @PostMapping
    public ResponseEntity<Questao> criar(@Valid @RequestBody Questao questao) {
        if (questao.getDisciplina() != null && questao.getDisciplina().getId() != null) {
            disciplinaService.buscarPorId(questao.getDisciplina().getId())
                .ifPresent(questao::setDisciplina);
        }
        if (questao.getSerie() != null && questao.getSerie().getId() != null) {
            serieService.buscarPorId(questao.getSerie().getId())
                .ifPresent(questao::setSerie);
        }
        Questao saved = service.salvar(questao);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questao> atualizar(@PathVariable Integer id, @Valid @RequestBody Questao questaoAtualizada) {
        return service.buscarPorId(id).map(questao -> {
            questao.setEnunciado(questaoAtualizada.getEnunciado());
            questao.setDificuldade(questaoAtualizada.getDificuldade());
            
            if (questaoAtualizada.getDisciplina() != null && questaoAtualizada.getDisciplina().getId() != null) {
                disciplinaService.buscarPorId(questaoAtualizada.getDisciplina().getId())
                    .ifPresent(questao::setDisciplina);
            }
            
            if (questaoAtualizada.getSerie() != null && questaoAtualizada.getSerie().getId() != null) {
                serieService.buscarPorId(questaoAtualizada.getSerie().getId())
                    .ifPresent(questao::setSerie);
            }
            
            return ResponseEntity.ok(service.salvar(questao));
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