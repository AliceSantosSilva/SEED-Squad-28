package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Prova;
import com.projeto.sistema_escolar.service.ProvaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/provas")
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
    public ResponseEntity<Prova> buscarPorId(@PathVariable Integer id) {
        Optional<Prova> prova = service.buscarPorId(id);
        return prova.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/professor/{professorId}")
    public List<Prova> buscarPorProfessor(@PathVariable Integer professorId) {
        return service.buscarPorProfessor(professorId);
    }

    @PostMapping
    public ResponseEntity<Prova> criar(@Valid @RequestBody Prova prova) {
        Prova saved = service.salvar(prova);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Prova> atualizar(@PathVariable Integer id, @Valid @RequestBody Prova provaAtualizada) {
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
            prova.setTurma(provaAtualizada.getTurma());
            prova.setQuestoes(provaAtualizada.getQuestoes());
            return ResponseEntity.ok(service.salvar(prova));
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

    @PostMapping("/gerar")
    public ResponseEntity<Prova> gerarProvaAutomatica(
            @RequestParam Integer disciplinaId,
            @RequestParam Integer serieId,
            @RequestParam Integer quantidadeQuestoes,
            @Valid @RequestBody Prova provaBase) {

        Prova provaGerada = service.gerarProvaAutomatica(provaBase, disciplinaId, serieId, quantidadeQuestoes);
        return ResponseEntity.status(HttpStatus.CREATED).body(provaGerada);
    }
}