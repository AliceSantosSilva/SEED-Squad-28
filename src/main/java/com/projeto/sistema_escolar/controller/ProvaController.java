package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.service.ProvaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/provas")
@CrossOrigin(origins = "*")
public class ProvaController {

    private final ProvaService service;

    public ProvaController(ProvaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Prova> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prova> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/professor/{professorId}")
    public List<Prova> buscarPorProfessor(@PathVariable Long professorId) {
        return service.buscarPorProfessor(professorId);
    }

    @PostMapping
    public Prova criar(@RequestBody Prova prova) {
        return service.salvar(prova);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prova> atualizar(@PathVariable Long id, @RequestBody Prova provaAtualizada) {
        return service.buscarPorId(id).map(prova -> {
            prova.setTitulo(provaAtualizada.getTitulo());
            prova.setDataInicio(provaAtualizada.getDataInicio());
            prova.setDataFim(provaAtualizada.getDataFim());
            prova.setDuracaoMinutos(provaAtualizada.getDuracaoMinutos());
            prova.setTipo(provaAtualizada.getTipo());
            prova.setModalidade(provaAtualizada.getModalidade());
            prova.setNotaMinimaAprovacao(provaAtualizada.getNotaMinimaAprovacao());
            prova.setGerarVariacoes(provaAtualizada.isGerarVariacoes());
            prova.setProfessor(provaAtualizada.getProfessor());
            prova.setQuestoes(provaAtualizada.getQuestoes());
            return ResponseEntity.ok(service.salvar(prova));
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