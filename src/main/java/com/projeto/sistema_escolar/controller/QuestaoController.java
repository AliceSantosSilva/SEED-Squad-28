package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.service.QuestaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questoes")
@CrossOrigin(origins = "*")
public class QuestaoController {

    private final QuestaoService service;

    public QuestaoController(QuestaoService service) {
        this.service = service;
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
    public Questao criar(@RequestBody Questao questao) {
        return service.salvar(questao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questao> atualizar(@PathVariable Long id, @RequestBody Questao questaoAtualizada) {
        return service.buscarPorId(id).map(questao -> {
            questao.setEnunciado(questaoAtualizada.getEnunciado());
            questao.setDificuldade(questaoAtualizada.getDificuldade());
            questao.setDisciplina(questaoAtualizada.getDisciplina());
            questao.setSerie(questaoAtualizada.getSerie());
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