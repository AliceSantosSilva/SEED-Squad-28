package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.dto.DisciplinaRequestDTO;
import com.projeto.sistema_escolar.model.Disciplina;
import com.projeto.sistema_escolar.service.DisciplinaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/disciplinas")
@CrossOrigin(origins = "*")
public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Disciplina> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Disciplina> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Disciplina> criar(@Valid @RequestBody DisciplinaRequestDTO dto) {

        Disciplina disciplina = new Disciplina();


        disciplina.setNome(dto.getNome());


        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(disciplina));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Disciplina> atualizar(@PathVariable Long id, @Valid @RequestBody DisciplinaRequestDTO dto) {
        return service.buscarPorId(id).map(disciplina -> {

            disciplina.setNome(dto.getNome());

            return ResponseEntity.ok(service.salvar(disciplina));
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