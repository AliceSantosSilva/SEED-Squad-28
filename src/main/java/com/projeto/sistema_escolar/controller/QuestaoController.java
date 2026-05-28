package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.QuestaoRequestDTO;
import com.projeto.sistema_escolar.model.Questao;
import com.projeto.sistema_escolar.service.QuestaoService;
import jakarta.validation.Valid;
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
    public ResponseEntity<Questao> criar(@Valid @RequestBody QuestaoRequestDTO dto) {
        Questao questao = new Questao();
        questao.setEnunciado(dto.getEnunciado());
        questao.setDificuldade(dto.getDificuldade());


        com.projeto.sistema_escolar.model.Disciplina disciplina = new com.projeto.sistema_escolar.model.Disciplina();
        disciplina.setId(dto.getDisciplinaId());
        questao.setDisciplina(disciplina);


        com.projeto.sistema_escolar.model.Serie serie = new com.projeto.sistema_escolar.model.Serie();
        serie.setId(dto.getSerieId());
        questao.setSerie(serie);

        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(service.salvar(questao));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Questao> atualizar(@PathVariable Long id, @Valid @RequestBody QuestaoRequestDTO dto) {
        return service.buscarPorId(id).map(questao -> {
            questao.setEnunciado(dto.getEnunciado());
            questao.setDificuldade(dto.getDificuldade());

            com.projeto.sistema_escolar.model.Disciplina disciplina = new com.projeto.sistema_escolar.model.Disciplina();
            disciplina.setId(dto.getDisciplinaId());
            questao.setDisciplina(disciplina);

            com.projeto.sistema_escolar.model.Serie serie = new com.projeto.sistema_escolar.model.Serie();
            serie.setId(dto.getSerieId());
            questao.setSerie(serie);

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