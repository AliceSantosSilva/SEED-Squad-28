package com.projeto.sistema_escolar.controller;

import com.projeto.sistema_escolar.model.Alternativa;
import com.projeto.sistema_escolar.service.AlternativaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alternativas")
public class AlternativaController {

    private final AlternativaService service;

    public AlternativaController(AlternativaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Alternativa> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alternativa> buscarPorId(@PathVariable Integer id) {
        Optional<Alternativa> alternativa = service.buscarPorId(id);
        return alternativa.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/questao/{questaoId}")
    public List<Alternativa> buscarPorQuestao(@PathVariable Integer questaoId) {
        return service.buscarPorQuestao(questaoId);
    }

    @PostMapping
    public ResponseEntity<Alternativa> criar(@Valid @RequestBody Alternativa alternativa) {
        Alternativa saved = service.salvar(alternativa);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Alternativa> atualizar(@PathVariable Integer id, @Valid @RequestBody Alternativa altAtualizada) {
        return service.buscarPorId(id).map(alt -> {
            alt.setTexto(altAtualizada.getTexto());
            alt.setCorreta(altAtualizada.isCorreta());
            alt.setQuestao(altAtualizada.getQuestao());
            return ResponseEntity.ok(service.salvar(alt));
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